#!/usr/bin/env bash
#
# Vinyler 로드맵 → GitHub Milestones + Issues 일괄 생성 스크립트
#
# 사전 준비:
#   1) GitHub CLI 설치        : brew install gh
#   2) 로그인                 : gh auth login
#   3) 실행                   : bash scripts/create_github_issues.sh
#
# 멱등성: 이미 존재하는 마일스톤/라벨은 건너뜁니다. (이슈는 중복 생성될 수 있으니 1회만 실행 권장)

set -euo pipefail

REPO="miiiiiin/Vinyler-backend"

echo "==> Repo: $REPO"

# ---------------------------------------------------------------------------
# 1. 라벨 생성 (--force: 있으면 갱신)
# ---------------------------------------------------------------------------
echo "==> 라벨 생성"
gh label create "bug"      --repo "$REPO" --color "d73a4a" --description "버그 수정"        --force
gh label create "refactor" --repo "$REPO" --color "fbca04" --description "코드 품질/리팩토링" --force
gh label create "feature"  --repo "$REPO" --color "0e8a16" --description "신규 기능"        --force
gh label create "infra"    --repo "$REPO" --color "1d76db" --description "인프라/설정"       --force
gh label create "ai"       --repo "$REPO" --color "5319e7" --description "AI/RAG"          --force

# ---------------------------------------------------------------------------
# 2. 마일스톤 생성 (이미 있으면 무시)
# ---------------------------------------------------------------------------
create_milestone () {
  local title="$1" due="$2" desc="$3"
  if [[ -n "$due" ]]; then
    gh api "repos/$REPO/milestones" -f title="$title" -f due_on="$due" -f description="$desc" >/dev/null 2>&1 \
      && echo "    + $title" || echo "    = $title (이미 존재)"
  else
    gh api "repos/$REPO/milestones" -f title="$title" -f description="$desc" >/dev/null 2>&1 \
      && echo "    + $title" || echo "    = $title (이미 존재)"
  fi
}

echo "==> 마일스톤 생성"
M0="Phase 0: 리팩토링"
M1="Phase 1: Search (Elasticsearch)"
M2="Phase 2: Event + 인기집계"
M3="Phase 3: Batch 분리"
M4="Phase 4: Notification"
M5="Phase 5: AI/RAG"

create_milestone "$M0" "2026-06-30T23:59:59Z" "버그/품질/미구현/페이징 리팩토링"
create_milestone "$M1" "2026-07-07T23:59:59Z" "Elasticsearch 검색 (Nori, fuzzy, 자동완성)"
create_milestone "$M2" "2026-07-14T23:59:59Z" "Domain Event + Redis Stream / 인기 LP 집계"
create_milestone "$M3" "2026-07-21T23:59:59Z" "배치 서버 분리 + 임베딩 색인 잡 선반영"
create_milestone "$M4" "2026-07-28T23:59:59Z" "가격 변동 감지 + SSE 알림"
create_milestone "$M5" ""                      "Qdrant + RAG 추천 (차차)"

# ---------------------------------------------------------------------------
# 3. 이슈 생성
# ---------------------------------------------------------------------------
issue () {
  # $1 title  $2 body  $3 milestone  $4 label
  gh issue create --repo "$REPO" --title "$1" --body "$2" --milestone "$3" --label "$4" >/dev/null \
    && echo "    + [$4] $1"
}

echo "==> 이슈 생성 (Phase 0)"
issue "버그: 팔로우 카운트 오류 수정" "currentUser.getFollowingsCount() + 1 로직 수정" "$M0" "bug"
issue "버그: 팔로잉 목록 조회 오류 수정" "findByFollowing → findByFollower 로 수정" "$M0" "bug"
issue "버그: getVinylsListenedByUser 유저 ID 로직(TODO) 처리" "감상 목록 조회 시 유저 ID 로직 확인 및 수정" "$M0" "bug"
issue "품질: @Transactional 추가" "VinylServiceImpl, ReviewServiceImpl 에 트랜잭션 경계 추가" "$M0" "refactor"
issue "품질: modifiedDate 중복 제거" "ReviewServiceImpl.updateReview 의 modifiedDate 중복 설정 제거" "$M0" "refactor"
issue "품질: User.equals() 수정" "equals 비교에서 password 필드 제외" "$M0" "refactor"
issue "품질: SliceResponse Raw type 제거" "제네릭 타입 명시" "$M0" "refactor"
issue "품질: UserController 불필요한 import 제거" "사용하지 않는 import 정리" "$M0" "refactor"
issue "리팩토링: Entity → DTO 의존 제거" "Vinyl.of(LikeRequestDto) 팩토리 제거, 변환 로직을 서비스 레이어로 이동" "$M0" "refactor"
issue "기능: 찜/감상 목록 접근 제어" "본인 또는 팔로우한 사용자만 조회 가능하도록 검증 로직 추가" "$M0" "feature"
issue "기능: 유저 정보 수정 API" "PATCH /api/v1/user (닉네임, 프로필, 생일)" "$M0" "feature"
issue "기능: 유저 탈퇴 API" "DELETE /api/v1/user (soft delete 활용)" "$M0" "feature"
issue "기능: 리뷰 삭제 API" "DELETE /api/v1/reviews/{reviewId} (본인만 가능)" "$M0" "feature"
issue "기능: 감상한 음반 목록 커서 페이징" "getVinylsListenedByUser 커서 기반 페이징 적용" "$M0" "feature"
issue "기능: 팔로워/팔로잉 목록 커서 페이징" "전체 조회 → 커서 기반 페이징 적용" "$M0" "feature"
issue "결정: Kakao OAuth2 활성화 여부" "활성화 or 코드 정리 결정" "$M0" "refactor"
issue "마무리: 전체 테스트 및 Swagger 확인" "회귀 테스트 + Swagger UI 점검" "$M0" "refactor"

echo "==> 이슈 생성 (Phase 1 - Search)"
issue "ES 환경 구성" "Docker Compose 에 Elasticsearch + Nori(한글 형태소) 추가" "$M1" "infra"
issue "Vinyl 색인 매핑 설계" "artist/title/genre/year/country 문서 매핑" "$M1" "feature"
issue "DB → ES 초기 색인" "기존 Vinyl 데이터 일괄 색인" "$M1" "feature"
issue "검색 API 구현" "멀티필드 검색 + fuzzy(오타 허용)" "$M1" "feature"
issue "자동완성 API" "edge n-gram 기반 자동완성" "$M1" "feature"

echo "==> 이슈 생성 (Phase 2 - Event + 인기집계)"
issue "Domain Event 정의" "좋아요/리뷰작성 등 2~3종 이벤트 발행 (Spring ApplicationEvent)" "$M2" "feature"
issue "Redis Stream 연동" "이벤트 → Redis Stream 프로듀서/컨슈머(consumer group 1개)" "$M2" "infra"
issue "이벤트 멱등성 처리(최소)" "중복 소비 방지 기본 처리" "$M2" "feature"
issue "인기 LP 집계" "이벤트 컨슈머 → Redis Sorted Set ZADD, 인기 랭킹 API" "$M2" "feature"

echo "==> 이슈 생성 (Phase 3 - Batch)"
issue "vinyler-batch 프로젝트 분리" "Spring Batch 신규 프로젝트 셋업" "$M3" "infra"
issue "리뷰 집계 배치" "rating 평균/분포/통계 → Vinyl 비정규화" "$M3" "feature"
issue "장르 트렌드 집계 배치" "일배치 장르별 트렌드 계산" "$M3" "feature"
issue "ES 증분 색인 배치" "DB → Elasticsearch 변경분 동기화" "$M3" "feature"
issue "임베딩 색인 잡(선반영)" "신규 LP/리뷰 → 임베딩 생성 → Qdrant 적재 (RAG용 데이터 적재)" "$M3" "ai"

echo "==> 이슈 생성 (Phase 4 - Notification)"
issue "Discogs 가격 수집 배치" "가격 수집 (vinyler-batch)" "$M4" "feature"
issue "가격 변동 감지" "변동 임계치 감지 → 이벤트 발행" "$M4" "feature"
issue "SSE 실시간 알림" "Server-Sent Events 알림 전송" "$M4" "feature"
issue "알림 이력 저장 + 조회 API" "알림 엔티티 + 조회 API" "$M4" "feature"

echo "==> 이슈 생성 (Phase 5 - AI/RAG)"
issue "Qdrant 구성" "Docker Compose 에 Qdrant 추가 + 컬렉션 설계" "$M5" "ai"
issue "의미 검색 API" "질문 임베딩 → Vector Search (유사 LP/리뷰)" "$M5" "ai"
issue "RAG 추천" "Vector Search 결과 → GPT 추천 문장 생성 (+ES 하이브리드 보완)" "$M5" "ai"
issue "AI 리뷰 요약" "음반별 리뷰 → LLM 요약(장점/단점), 배치 사전 생성" "$M5" "ai"

echo "==> 완료!  https://github.com/$REPO/milestones 에서 확인하세요."
