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

### 페이징 패턴

리스트 조회는 커서 기반 페이징을 사용. `size+1`개를 조회해 `hasNext` 판단 후 마지막 요소 제거. 응답은 `SliceResponse<T>` (content, hasNext, nextCursorId, size) 형태로 반환.

### 예외 처리

도메인별 예외 클래스는 모두 `ClientErrorException`을 상속. `GlobalExceptionHandler`에서 `@RestControllerAdvice`로 일괄 처리. 내부 서버 오류(`RuntimeException`, `Exception`)는 500 상태 코드만 반환 (메시지 미노출).

### Service 인터페이스 패턴

서비스 레이어는 인터페이스(`VinylService`, `ReviewService` 등) + 구현체(`VinylServiceImpl`, `ReviewServiceImpl` 등) 구조.

### Kakao OAuth2

`application-dev.yml`에 설정. `SocialOAuth2UserService` + `SocialOAuth2User`로 구현. `SecurityConfig`에서 oauth2Login 설정이 주석 처리되어 있어 현재 비활성 상태.
