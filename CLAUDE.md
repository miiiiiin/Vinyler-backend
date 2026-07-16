# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# 빌드
./gradlew build

# 실행 (기본: application.yml 기준)
./gradlew bootRun

# dev 프로파일로 실행 (Kakao OAuth 포함)
./gradlew bootRun --args='--spring.profiles.active=dev'

# 테스트 전체 실행 (ES 테스트가 TestContainers를 쓰므로 Docker 데몬 필요)
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "miiiiiin.com.vinyler.VinylerServerApplicationTests"

# 컴파일만 빠르게 검증 (수정 후 1차 확인용)
./gradlew compileJava compileTestJava

# Redis + Postgres + Elasticsearch 로컬 실행
docker-compose up -d
```

## 인프라 요구사항
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Elasticsearch: `http://localhost:9200` — 공식 이미지가 아닌 커스텀 빌드가 필요 (`docker-compose build elasticsearch`), 자세한 내용은 아래 [Elasticsearch 검색](#elasticsearch-검색) 참고
- DB 스키마는 **Flyway로 관리**한다 (`src/main/resources/db/migration`). 엔티티 필드를 추가·삭제하면 `V{n}__{설명}.sql` 마이그레이션을 반드시 함께 작성할 것 — 기존 파일은 절대 수정하지 말고 새 버전을 추가한다.

## 아키텍처 개요

**Vinyler**는 LP 음반(바이닐) 소셜 플랫폼 백엔드. Discogs API의 음반 데이터를 기반으로 찜, 감상, 리뷰, 팔로우 기능을 제공한다.

### 패키지 구조

```
miiiiiin.com.vinyler
├── auth/           # 인증 (JWT 발급/검증, 로그인 필터, 토큰 재발급)
├── config/         # Spring Security, Redis, Swagger 설정
├── user/           # 회원가입, 유저 정보, 팔로우/언팔로우
├── application/    # 핵심 도메인 (Vinyl, Like, Review, UserVinylStatus, Follow)
│   ├── document/   #   VinylDocument (Elasticsearch 색인 문서)
│   ├── event/      #   랭킹 집계 이벤트 + Redis Stream Producer/Consumer, ES 색인 동기화
│   └── ...         #   controller / dto / entity / repository / service
├── discogs/        # Discogs API 프록시 (client, config, service)
├── security/       # UserDetailsImpl (Spring Security 인증 주체)
├── exception/      # 도메인별 예외 클래스 + GlobalExceptionHandler
├── error/          # ErrorResponse DTO
└── global/         # GlobalResponseDto, Constants
```

### 인증 흐름

1. **로그인**: `POST /api/*/auth/login` → `CustomUsernamePasswordAuthenticationFilter` 처리 → Access Token(헤더) + Refresh Token(HttpOnly 쿠키) 발급, Refresh Token은 Redis에 `email → refreshToken` 형태로 저장
2. **요청 인증**: `JwtVerificationFilter` (OncePerRequestFilter) → Authorization 헤더에서 Bearer 토큰 추출 → 유효하면 SecurityContext에 `UserDetailsImpl` 세팅
3. **토큰 재발급**: `PATCH /api/v1/auth/reissue` — Authorization 헤더(AT) + 쿠키(RT) 전달 → Redis에서 RT 검증 → 새 AT 발급
4. **로그아웃**: `POST /api/v1/auth/logout` — Redis에서 RT 삭제 + 쿠키 초기화
5. **JWT 예외 처리**: `JwtExceptionFilter` (JwtVerificationFilter 다음에 등록) → JWT 관련 예외를 JSON 응답으로 변환

공개 엔드포인트: `POST /api/*/user/register`, `POST /api/*/auth/login`, `PATCH /api/v1/auth/reissue`

### 핵심 도메인 관계

- **Vinyl**: Discogs Release ID(`discogsId`)를 unique key로 사용. 클라이언트가 찜/감상 요청 시 DB에 없으면 자동 생성됨. **Discogs ToS 대응으로 경량화된 엔티티** — Discogs 유래 필드는 `discogsId`, `title`, `artistsSort`, `releasedFormatted`가 전부다(`likesCount`/`reviewsCount`는 집계값, `user`/`likes`는 FK 연관관계). 트랙리스트·이미지·포맷 등 풍부한 콘텐츠는 DB에 없으며 Discogs 실시간 조회 + Redis 캐시로 가져온다 (아래 [상세 조회 캐싱](#상세-조회-캐싱-discogs-프록시--redis-95) 참고). 여기에 필드를 추가하려 할 때는 ToS 위반이 아닌지 먼저 확인할 것
- **Like**: User × Vinyl 복합 unique 인덱스. `toggleLike()` — 이미 찜했으면 삭제, 없으면 생성. `Vinyl.likesCount`를 함께 업데이트
- **UserVinylStatus**: `listened` (감상 여부), `liked` (찜 여부) 플래그. `Review` 생성 선행 조건이었으나 현재 비활성화됨 (주석 처리)
- **Review**: User × Vinyl 복합 unique 제약 (한 유저, 한 음반, 하나의 리뷰). 커서 기반 페이징(`findByVinylWithCursor`) 사용
- **Follow**: `follower` → `following` 방향. `User.followersCount` / `followingsCount` 카운트 컬럼을 직접 관리
- **User**: soft delete (`@SQLDelete` + `@Where(deleted_date IS NULL)`)

### Discogs API 데이터 라이선스 및 캐싱 정책

Discogs API 콘텐츠는 두 종류로 나뉘며, 종류에 따라 저장/사용 규칙이 다르다.

- **CC0 데이터** (CC0 라이선스, 재사용 제약 없음): 발매 제목·설명·날짜·형식·트랙리스트·바코드/식별자·크레딧·버전·URL, 아티스트 이름·설명·관련 발매, 레이블/프로듀서/제조사/유통사 이름·연락처·참고사항. Vinyl 엔티티가 다루는 데이터는 전부 이 범주에 속한다.
- **제한된 데이터** (CC0 아님): Discogs 유저 데이터(유저명, 유저 이미지, 실명/홈페이지/위치/약력/컬렉션/위시리스트), 마켓플레이스 데이터(재고·주문·리스팅·수수료·가격·판매 이력), 이미지(퍼블릭 도메인/CC0/공정사용 요건 미충족 시). **Vinyler는 현재 이 범주의 데이터를 다루지 않는다** (마켓플레이스 시세, Discogs 유저 프로필 연동 없음).

**저장/캐싱 공통 규칙** (CC0·제한 데이터 모두 적용, 이용약관 근거):
- Discogs 웹/앱에 게시된 정보보다 **6시간 이상 오래된 콘텐츠를 표시 금지**
- **서비스 제공에 필요한 기간을 초과하여 캐시/저장 금지** — "영구 캐시"로 취급하면 안 됨

**제한된 데이터 추가 제약** (CC0에는 없음): 제3자 전송 금지, 상업적 목적 사용 금지, 타인의 개인정보·지식재산권 침해 방식 사용 금지. (Discogs가 라이선스를 부여하는 대상이므로 저장 자체는 허용되나 위 용도로만 사용 가능)

**Vinyl 엔티티 설계 원칙** (위 규칙에서 도출, **V2·V3 마이그레이션으로 반영 완료**):
- `Vinyl`은 `Like`/`Review`/`UserVinylStatus`의 FK 앵커이기도 해서, 유저가 찜/리뷰한 음반의 row 자체를 삭제할 수는 없다. 다만 **영구 보존이 정당화되는 필드는 최소 식별 정보(`discogsId`, `title`, `artistsSort`, `releasedFormatted`)로 한정**한다 — "서비스 제공에 필요한" 데이터로 방어 가능한 범위. (`likesCount`/`reviewsCount`는 Discogs 데이터가 아닌 우리 서비스의 집계값이라 별개)
- `notes`, `tracklist`, `formats`, `artists`, `images`, `videos` 등 풍부한 콘텐츠는 FK 앵커 목적상 영구 저장이 필요하지 않다. `V2__vinyl_lightweight_and_drop_legacy_tables.sql`에서 하위 테이블(`images`, `tracklist`, `format`, `format_descriptions`, `artist_detail`, `video`)을 DROP했고, `V3__drop_vinyls_orphaned_columns.sql`에서 `notes`/`status`/`uri` 컬럼을 제거했다. 해당 데이터는 이제 **Discogs API 실시간 조회 + Redis TTL 5시간 캐싱**으로 제공된다.

**저작권/출처 표시 의무** (프론트엔드·API 문서 등 사용자 대면 영역에 반영 필요):
- "이 애플리케이션은 Discogs의 API를 사용하지만 Discogs와 제휴, 후원 또는 보증 관계가 없습니다. 'Discogs'는 Zink Media, LLC의 상표입니다." 문구를 이용약관/설명서에 표시
- Discogs API에서 가져온 데이터 옆에 "Discogs에서 제공하는 데이터입니다" 안내 + 해당 discogs.com 페이지로의 하이퍼링크(nofollow 등 검색엔진 배제 처리 금지) 포함

### 페이징 패턴

리스트 조회는 커서 기반 페이징을 사용. `size+1`개를 조회해 `hasNext` 판단 후 마지막 요소 제거. 응답은 `SliceResponse<T>` (content, hasNext, nextCursorId, size) 형태로 반환.

### 예외 처리

도메인별 예외 클래스는 모두 `ClientErrorException`을 상속. `GlobalExceptionHandler`에서 `@RestControllerAdvice`로 일괄 처리. 내부 서버 오류(`RuntimeException`, `Exception`)는 500 상태 코드만 반환 (메시지 미노출).

### Service 인터페이스 패턴

서비스 레이어는 인터페이스(`VinylService`, `ReviewService` 등) + 구현체(`VinylServiceImpl`, `ReviewServiceImpl` 등) 구조.

### Kakao OAuth2

`application-dev.yml`에 설정. `SocialOAuth2UserService` + `SocialOAuth2User`로 구현. `SecurityConfig`에서 oauth2Login 설정이 주석 처리되어 있어 현재 비활성 상태.

### Elasticsearch 검색

Vinyl 검색(키워드 검색 / 자동완성 / 인기 검색어)을 Elasticsearch로 제공한다. **구현 완료 상태.**

**구성요소**

| 클래스 | 역할 |
|---|---|
| `VinylDocument` | ES 색인 문서. `@Document(indexName="vinyls")` + `vinyl-settings.json` 세팅. `Vinyl` → 문서 변환은 `VinylDocument.from()` |
| `VinylerElasticSearchRepository` | `searchByKeyword(keyword, pageable)`, `autocomplete(prefix, pageable)` |
| `VinylElasticSearchServiceImpl` | 검색 실행 + 검색어를 `PopularKeywordService.record()`로 집계 |
| `PopularKeywordService` | 인기 검색어 집계 (Redis ZSET 시간 버킷) |
| `VinylIndexSyncListener` | DB → ES 색인 동기화 |

**필드 매핑 규칙** (`VinylDocument`)
- `title`, `artistsSort`: `FieldType.Text` + `analyzer="korean"` (Nori 형태소 분석 → 부분/유사 검색)
- `releasedFormatted`: `FieldType.Keyword` (토큰 분리 없이 정렬·필터용)
- `likesCount`, `reviewsCount`: `FieldType.Long` (정렬 기준. `VinylSortType.toSort()`가 조립)
- `suggest`: `FieldType.Search_As_You_Type` (`title + " " + artistsSort`를 합쳐 자동완성 전용으로 담음)

**쿼리 전략** (`VinylerElasticSearchRepository`의 `@Query`)
- 검색: `multi_match` on `["title^2", "artistsSort"]` + `fuzziness: AUTO` — 제목 가중치 2배, 오타 허용
- 자동완성: `multi_match` `type: bool_prefix` on `["suggest", "suggest._2gram", "suggest._3gram"]` — `Search_As_You_Type`이 자동 생성한 n-gram 서브필드를 사용하므로 `suggest` 필드 타입을 바꾸면 이 쿼리도 함께 깨진다

**DB → ES 동기화**
`VinylIndexSyncEvent` 발행 → `VinylIndexSyncListener`가 `@TransactionalEventListener(AFTER_COMMIT)` + `@Async("esSyncExecutor")`로 수신 → DB에서 최신 Vinyl 재조회 후 색인. 커밋 후에만 색인하므로 롤백된 데이터가 ES에 남지 않고, 비동기라 색인 지연이 API 응답을 막지 않는다. Vinyl이 없으면 색인을 건너뛰고 warn 로그만 남긴다.

**환경 구성** (`docker-compose.yml`, `docker/elasticsearch/Dockerfile`):
- ES 8.15.0 기반에 Nori(한국어 형태소 분석기) 플러그인을 설치한 커스텀 이미지를 빌드해서 사용. 공식 이미지엔 Nori가 기본 포함되어 있지 않아 Dockerfile로 `elasticsearch-plugin install analysis-nori`를 빌드 타임에 실행함
- 로컬 전용 설정: `xpack.security.enabled: false`로 인증 비활성화 (ES 8.x는 기본값이 `true`라 미설정 시 모든 요청이 401). **운영 배포 시 재검토 필요**
- `discovery.type: single-node`(단일 노드 부팅), `esdata` volume(컨테이너 재생성해도 색인 데이터 유지)
- Spring 연결: `application.yml`의 `spring.elasticsearch.uris: http://localhost:9200`
- 별도 네트워크(`es-bridge` 등) 불필요 — 같은 compose 파일 내 서비스는 기본 네트워크로 서비스명 통신 가능. Kibana/Logstash도 아직 미도입 (필요해지면 같은 이유로 커스텀 네트워크 없이 추가 가능)

**테스트**: ES 테스트는 TestContainers로 실제 ES 컨테이너를 띄운다 (`ElasticsearchTestContainerConfig`, `VinylSearchElasticsearchTest`). 실행하려면 Docker 데몬이 떠 있어야 한다.

**인기 검색어 설계** (`PopularKeywordService`)

검색이 발생할 때마다 **시간 단위 버킷 ZSET**(`search:keyword:yyyyMMddHH`)에 `ZINCRBY`로 +1하고 버킷에 TTL 3시간을 건다. 조회 시에는 최근 N시간치 버킷을 `ZUNIONSTORE`로 합산해 상위 N개를 반환한다.

| 선택 | 왜 |
|---|---|
| 시간 버킷 + TTL | "실시간 인기"라 오래된 검색은 자동으로 빠져야 함. TTL이 만료 처리를 대신해 별도 정리 작업 불필요 |
| 조회 시 ZUNIONSTORE | 윈도우(몇 시간치)를 조회 시점에 정할 수 있음 — 집계를 미리 고정하지 않음 |
| dest 키에 UUID | 동시 요청이 같은 임시 키를 덮어쓰는 간섭 방지. `finally`에서 삭제 |
| 키워드 normalize (trim + lowercase) | "재즈 " / "JAZZ" / "jazz"가 다른 항목으로 세는 것 방지 |

**API**
- `GET /api/v1/vinyls/search?keyword=재즈&sort=REVIEWS&page=0&size=10` — 키워드 검색 (`sort`: `REVIEWS` | `LIKES`, 기본 `REVIEWS`)
- `GET /api/v1/vinyls/search/autocomplete?prefix=재` — 자동완성 (상위 5개 고정)
- `GET /api/v1/vinyls/search/popular-keywords?limit=10&hours=2` — 인기 검색어 상위 N (rank, keyword, count)

### 인기 LP 랭킹 (이벤트 기반 집계)

**파이프라인**

```
좋아요/리뷰 → ApplicationEvent → Producer(AFTER_COMMIT, XADD)
→ Redis Stream → Consumer(Consumer Group, XREADGROUP)
→ 멱등성(SETNX) → ZINCRBY → Sorted Set → GET /popular(ZREVRANGE + DB 제목 조합)
```

**설계 선택과 이유**

| 선택 | 왜 |
|---|---|
| ApplicationEvent | 도메인 로직을 집계/인프라에서 분리 |
| @TransactionalEventListener(AFTER_COMMIT) | 롤백된 좋아요가 랭킹에 반영되는 것 방지 |
| Redis Stream | 컨슈머가 죽어도 이벤트 유실 X, 재처리 가능 |
| Consumer Group + 수동 ACK | "어디까지 읽었나" 기억 + 실패 시 pending 재처리 |
| SETNX(processed:{eventId}) | at-least-once 배달의 중복을 멱등하게 방어 |
| Sorted Set | 집계 때 정렬 선불 → 조회 O(logN+N) |

**한계 (트레이드오프)**
- exactly-once 아님: 커밋~XADD, 도장~집계 사이 좁은 유실/중복 창 존재 → 완전 보장은 Outbox 필요 (현재는 그 직전 단계)

**API**
- `GET /api/v1/vinyls/popular?limit=10` — 인기 음반 상위 N (rank, discogsId, score, title, artistsSort)

### 상세 조회 캐싱 (Discogs 프록시 + Redis, #95)

**왜**
모바일 앱이 상세 화면마다 Discogs API를 직접 호출 → (유저 수 × 조회 수)가 그대로 Discogs 호출량이 되어 rate limit(인증 시 분당 ~60회)에 쉽게 걸림. 클라이언트별 캐시는 기기마다 따로라 전체 호출량이 안 줄어듦.

**해결**
Discogs 호출 주체를 백엔드로 옮기고(프록시), 응답을 Redis에 공유 캐시(TTL 5h — ToS 상한 6h보다 보수적으로 고정). 같은 음반은 5시간 동안 캐시 HIT → Discogs 미호출. rate limit 방어 + Discogs ToS("6시간 초과 표시 금지 / 영구 저장 금지")를 동시에 충족.

**파이프라인**

```
GET /vinyls/{discogsId}
  → VinylDetailCacheService: Redis 확인
       HIT  → 캐시 JSON 파싱 후 반환 (Discogs 미호출)
       MISS → DiscogsClient 호출 → Redis SET (TTL 5h) → 반환
  → DiscogsReleaseDto(실시간 원본) + DB 메타(찜/리뷰 수, 내 상태) 합쳐
    VinylDetailResponse 반환
```

**구성요소**

| 클래스 | 역할 |
|---|---|
| DiscogsProperties | application.yml의 discogs.* 값 매핑 (base-url/token/user-agent) |
| DiscogsClientConfig | RestClient 빈 조립 (공통 헤더·타임아웃) |
| DiscogsClient | `GET /releases/{id}` 호출 + 상태별 예외 번역 |
| VinylDetailCacheService | Cache-Aside (Redis 확인 → MISS 시 호출·저장). 캐시는 원본 문자열, 파싱은 읽을 때 |

**설계 선택과 이유**

| 선택 | 왜 |
|---|---|
| 백엔드 프록시로 전환 | 클라이언트별 캐시는 공유 안 됨 → 서버 캐시라야 전체 호출량 감소 |
| Redis TTL 5h | rate limit 방어 + Discogs ToS 상한(6h)보다 여유를 두어 방어적으로 대응 |
| 캐시에 원본 JSON 문자열 저장 | DTO 바뀌어도 캐시 안 깨짐. 파싱 규칙은 읽을 때 최신 적용 |
| release로 묶은 응답 | Discogs 원본/우리 DB 메타 경계 명확 + 매핑 코드 없음 |
| 404→404, 그 외 실패→503 | "없는 음반"과 "Discogs 일시 장애(재시도 가능)"를 구분 |

**엣지 케이스**
- DB에 row 없는 음반: 존재 판단 주체가 DB→Discogs로 이동. 메타만 0/false로 채워 정상 오픈
- User-Agent 필수: 없으면 Discogs가 403. RestClient 빈에 defaultHeader로 고정
- 타임아웃: connect 2s / read 3s로 "Discogs 하나 느려서 앱 전체 멈춤" 방지

**API**
- `GET /api/v1/vinyls/{discogsId}` — 상세(캐시된 Discogs 원본 + DB 메타)
