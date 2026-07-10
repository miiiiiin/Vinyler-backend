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

# 테스트 전체 실행
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "miiiiiin.com.vinyler.VinylerServerApplicationTests"

# Redis 로컬 실행 (docker-compose 필요)
docker-compose up -d
```

## 인프라 요구사항
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Elasticsearch: `http://localhost:9200` — 공식 이미지가 아닌 커스텀 빌드가 필요 (`docker-compose build elasticsearch`), 자세한 내용은 아래 [Elasticsearch 검색](#elasticsearch-검색-진행-중) 참고

## 아키텍처 개요

**Vinyler**는 LP 음반(바이닐) 소셜 플랫폼 백엔드. Discogs API의 음반 데이터를 기반으로 찜, 감상, 리뷰, 팔로우 기능을 제공한다.

### 패키지 구조

```
miiiiiin.com.vinyler
├── auth/           # 인증 (JWT 발급/검증, 로그인 필터, 토큰 재발급)
├── config/         # Spring Security, Redis, Swagger 설정
├── user/           # 회원가입, 유저 정보, 팔로우/언팔로우
├── application/    # 핵심 도메인 (Vinyl, Like, Review, UserVinylStatus, Follow)
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

- **Vinyl**: Discogs Release ID(`discogsId`)를 unique key로 사용. 클라이언트가 찜/감상 요청 시 DB에 없으면 자동 생성됨. 하위 엔티티(Image, TrackList, Format, ArtistDetail, Video)와 CascadeAll 관계
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

**Vinyl 엔티티 설계 원칙** (위 규칙에서 도출, 현재 코드는 미반영 상태 — 리팩토링 필요):
- `Vinyl`은 `Like`/`Review`/`UserVinylStatus`의 FK 앵커이기도 해서, 유저가 찜/리뷰한 음반의 row 자체를 삭제할 수는 없다. 다만 **영구 보존이 정당화되는 필드는 최소 식별 정보(`discogsId`, `title`, `artistsSort`, `releasedFormatted`, `uri`)로 한정**해야 한다 — "서비스 제공에 필요한" 데이터로 방어 가능한 범위.
- `notes`, `tracklist`, `formats`, `artists`, `images`, `videos` 등 풍부한 콘텐츠는 FK 앵커 목적상 영구 저장이 필요하지 않다. 현재는 `CascadeType.ALL`로 Postgres에 영구 저장되고 있는데(하위 엔티티: Image, TrackList, Format, ArtistDetail, Video), 이는 "필요 기간 초과 저장" 리스크가 있으므로 **Discogs API 실시간 조회 또는 Redis TTL ≤ 6시간 캐싱으로 전환**하는 방향이 바람직하다.

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

### Elasticsearch 검색 (진행 중)

Vinyl 검색 기능을 위해 Elasticsearch 도입 중. **로컬 개발 환경 구성은 완료, 색인/검색 애플리케이션 코드는 미착수.**

**환경 구성** (`docker-compose.yml`, `docker/elasticsearch/Dockerfile`):
- ES 8.15.0 기반에 Nori(한국어 형태소 분석기) 플러그인을 설치한 커스텀 이미지를 빌드해서 사용. 공식 이미지엔 Nori가 기본 포함되어 있지 않아 Dockerfile로 `elasticsearch-plugin install analysis-nori`를 빌드 타임에 실행함
- 로컬 전용 설정: `xpack.security.enabled: false`로 인증 비활성화 (ES 8.x는 기본값이 `true`라 미설정 시 모든 요청이 401). **운영 배포 시 재검토 필요**
- `discovery.type: single-node`(단일 노드 부팅), `esdata` volume(컨테이너 재생성해도 색인 데이터 유지)
- Spring 연결: `application.yml`의 `spring.elasticsearch.uris: http://localhost:9200`
- 별도 네트워크(`es-bridge` 등) 불필요 — 같은 compose 파일 내 서비스는 기본 네트워크로 서비스명 통신 가능. Kibana/Logstash도 아직 미도입 (필요해지면 같은 이유로 커스텀 네트워크 없이 추가 가능)

**남은 작업**: `VinylDocument`(Nori 매핑 포함 인덱스 설계), `VinylSearchRepository`, Vinyl 생성/수정 시 ES 동기화 로직(`VinylServiceImpl`), 검색 API 컨트롤러

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
