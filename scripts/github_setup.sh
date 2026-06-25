#!/usr/bin/env bash
#
# Vinyler 로드맵 → GitHub Milestones + Issues 일괄 생성 스크립트
#
# 사전 준비:
#   1. gh CLI 설치:  brew install gh
#   2. 인증:         gh auth login   (GitHub.com → HTTPS → 브라우저 인증)
#   3. 실행:         bash scripts/github_setup.sh
#
# 주의: 한 번만 실행하세요. 재실행하면 이슈/마일스톤이 중복 생성됩니다.
#       (마일스톤은 중복 title이면 스킵하도록 처리되어 있습니다.)

set -euo pipefail

REPO="miiiiiin/Vinyler-backend"

echo "==> Repo: $REPO"

# ----------------------------------------------------------------------------
# 1) 라벨 생성 (이미 있으면 --force 로 덮어씀)
# ----------------------------------------------------------------------------
echo "==> Creating labels..."
create_label() { gh label create "$1" --repo "$REPO" --color "$2" --description "$3" --force >/dev/null; }

create_label "bug"          "d73a4a" "버그 수정"
create_label "refactor"     "fbca04" "리팩토링/코드 품질"
create_label "feature"      "0e8a16" "신규 기능"
create_label "search"       "1d76db" "Elasticsearch / 검색"
create_label "event"        "5319e7" "Domain Event / Redis Stream"
create_label "batch"        "006b75" "Spring Batch / 배치 서버"
create_label "notification" "c5def5" "알림 / SSE"
create_label "ai"           "bfd4f2" "AI / RAG / Qdrant"

# ----------------------------------------------------------------------------
# 2) 마일스톤 생성 (Phase별, due_on 포함)
# ----------------------------------------------------------------------------
echo "==> Creating milestones..."
create_milestone() {
  local title="$1" due="$2" desc="$3"
  # 동일 title 마일스톤이 이미 있으면 스킵
  if gh api "repos/$REPO/milestones?state=all" --jq '.[].title' | grep -Fxq "$title"; then
    echo "    (skip, exists) $title"
    return
  fi
  if [ -n "$due" ]; then
    gh api --method POST "repos/$REPO/milestones" \
      -f title="$title" -f description="$desc" -f due_on="${due}T23:59:59Z" >/dev/null
  else
    gh api --method POST "repos/$REPO/milestones" \
      -f title="$title" -f description="$desc" >/dev/null
  fi
  echo "    created: $title"
}

create_milestone "Phase 0: 리팩토링"      "2026-06-30" "버그/코드품질/미구현/페이징 정리 (6/26~6/30)"
create_milestone "Phase 1: Search"        "2026-07-07" "Elasticsearch 검색 (Nori, fuzzy, 자동완성)"
create_milestone "Phase 2: Event"         "2026-07-14" "Domain Event + Redis Stream / 인기 LP 집계"
create_milestone "Phase 3: Batch"         "2026-07-21" "배치 서버 분리 (+임베딩 색인 잡 선반영)"
create_milestone "Phase 4: Notification"  "2026-07-28" "가격 변동 감지 + SSE 알림"
create_milestone "Phase 5: AI/RAG"        ""           "Qdrant + RAG 추천 (차차)"

# ----------------------------------------------------------------------------
# 3) 이슈 생성
# ----------------------------------------------------------------------------
echo "==> Creating issues..."
new_issue() {
  local title="$1" milestone="$2" labels="$3" body="$4"
  gh issue create --repo "$REPO" \
    --title "$title" \
    --milestone "$milestone" \
    --label "$labels" \
    --body "$body" >/dev/null
  echo "    + $title"
}

# ---- Phase 0: 리팩토링 ----
new_issue "[버그] 팔로우 카운트 오류 수정" "Phase 0: 리팩토링" "bug" \
"\`currentUser.getFollowingsCount() + 1\` 로 카운트 보정. (6/26)"
new_issue "[버그] 팔로잉 목록 조회 오류 수정" "Phase 0: 리팩토링" "bug" \
"\`findByFollowing\` → \`findByFollower\` 로 수정. (6/26)"
new_issue "[버그] getVinylsListenedByUser 유저 ID 로직 TODO 처리" "Phase 0: 리팩토링" "bug" \
"감상 목록 조회 시 유저 ID 로직 확인 및 수정. (6/26)"
new_issue "[품질] @Transactional 추가" "Phase 0: 리팩토링" "refactor" \
"\`VinylServiceImpl\`, \`ReviewServiceImpl\` 에 트랜잭션 경계 추가. (6/26)"
new_issue "[품질] modifiedDate 중복 제거" "Phase 0: 리팩토링" "refactor" \
"\`ReviewServiceImpl.updateReview\` 의 modifiedDate 중복 설정 제거. (6/26)"
new_issue "[품질] User.equals() 수정" "Phase 0: 리팩토링" "refactor" \
"equals 비교에서 password 필드 제외. (6/26)"
new_issue "[품질] SliceResponse 제네릭 타입 명시 (raw type 제거)" "Phase 0: 리팩토링" "refactor" \
"raw type 사용 제거하고 제네릭 타입 명시. (6/26)"
new_issue "[품질] UserController 불필요한 import 제거" "Phase 0: 리팩토링" "refactor" \
"미사용 import 정리. (6/26)"
new_issue "Entity → DTO 의존 제거" "Phase 0: 리팩토링" "refactor" \
"\`Vinyl.of(LikeRequestDto)\` 팩토리 제거, 변환 로직을 서비스 레이어로 이동. (6/28)"
new_issue "찜/감상 목록 접근 제어 구현" "Phase 0: 리팩토링" "refactor,feature" \
"본인 또는 팔로우한 사용자만 조회 가능하도록 검증 로직 추가. (6/28)"
new_issue "유저 정보 수정 API" "Phase 0: 리팩토링" "feature" \
"\`PATCH /api/v1/user\` (닉네임, 프로필, 생일). (6/29)"
new_issue "유저 탈퇴 API" "Phase 0: 리팩토링" "feature" \
"\`DELETE /api/v1/user\` (soft delete 활용). (6/29)"
new_issue "리뷰 삭제 API" "Phase 0: 리팩토링" "feature" \
"\`DELETE /api/v1/reviews/{reviewId}\` (본인만 가능). (6/29)"
new_issue "감상한 음반 목록 커서 페이징" "Phase 0: 리팩토링" "refactor" \
"\`getVinylsListenedByUser\` 커서 기반 페이징 적용. (6/30)"
new_issue "팔로워/팔로잉 목록 커서 페이징" "Phase 0: 리팩토링" "refactor" \
"전체 조회 → 커서 기반 페이징으로 전환. (6/30)"
new_issue "Kakao OAuth2 활성화 여부 결정" "Phase 0: 리팩토링" "refactor" \
"활성화 또는 코드 정리 결정. (6/30)"
new_issue "전체 테스트 및 Swagger 확인" "Phase 0: 리팩토링" "refactor" \
"회귀 테스트 + Swagger UI 점검. (6/30)"

# ---- Phase 1: Search ----
new_issue "ES + Nori Docker 구성" "Phase 1: Search" "search" \
"docker-compose 에 Elasticsearch + Nori(한글 형태소) 추가."
new_issue "Vinyl 색인 매핑 설계" "Phase 1: Search" "search" \
"artist/title/genre/year/country 매핑 설계."
new_issue "DB → ES 초기 색인" "Phase 1: Search" "search" \
"기존 Vinyl 데이터 일괄 색인."
new_issue "멀티필드 + fuzzy 검색 API" "Phase 1: Search" "search,feature" \
"멀티필드 검색 + 오타 허용(fuzzy) 검색 API 구현."
new_issue "자동완성(edge n-gram) API" "Phase 1: Search" "search,feature" \
"edge n-gram 기반 자동완성 API 구현."

# ---- Phase 2: Event ----
new_issue "Domain Event 정의" "Phase 2: Event" "event" \
"좋아요/리뷰작성 등 2~3종 이벤트 발행 (Spring ApplicationEvent)."
new_issue "Redis Stream 프로듀서/컨슈머 연동" "Phase 2: Event" "event" \
"이벤트 → Redis Stream 프로듀서/컨슈머(consumer group 1개) 구성."
new_issue "이벤트 멱등성 처리(최소)" "Phase 2: Event" "event" \
"중복 소비 방지 기본 처리."
new_issue "인기 LP 집계 (Redis Sorted Set)" "Phase 2: Event" "event,feature" \
"이벤트 컨슈머 → ZADD 로 인기 랭킹 갱신, 랭킹 조회 API."

# ---- Phase 3: Batch ----
new_issue "vinyler-batch 프로젝트 분리" "Phase 3: Batch" "batch" \
"별도 Spring Batch 프로젝트 셋업."
new_issue "리뷰 집계 배치" "Phase 3: Batch" "batch" \
"rating 평균/분포/통계 → Vinyl 비정규화."
new_issue "장르 트렌드 집계 배치" "Phase 3: Batch" "batch" \
"일배치 장르별 트렌드 계산."
new_issue "ES 증분 색인 배치" "Phase 3: Batch" "batch,search" \
"DB → Elasticsearch 변경분 동기화."
new_issue "임베딩 색인 잡 (RAG 데이터 선적재)" "Phase 3: Batch" "batch,ai" \
"신규 LP/리뷰 → 임베딩 생성 → Qdrant 적재. (RAG 디리스크용)"

# ---- Phase 4: Notification ----
new_issue "가격 수집 배치" "Phase 4: Notification" "batch,notification" \
"Discogs 가격 수집 (vinyler-batch)."
new_issue "가격 변동 감지 → 이벤트 발행" "Phase 4: Notification" "notification,event" \
"변동 임계치 감지 → 이벤트 발행."
new_issue "SSE 알림 구현" "Phase 4: Notification" "notification,feature" \
"Server-Sent Events 실시간 알림 전송."
new_issue "알림 이력 저장 + 조회 API" "Phase 4: Notification" "notification,feature" \
"알림 엔티티 + 조회 API."

# ---- Phase 5: AI/RAG ----
new_issue "Qdrant 구성" "Phase 5: AI/RAG" "ai" \
"docker-compose 에 Qdrant 추가 + 컬렉션 설계."
new_issue "의미 검색 API" "Phase 5: AI/RAG" "ai,feature" \
"질문 임베딩 → Vector Search (유사 LP/리뷰)."
new_issue "RAG 추천 (GPT)" "Phase 5: AI/RAG" "ai,feature" \
"Vector Search 결과 → GPT 추천 문장 생성 (+ES 하이브리드 보완)."
new_issue "AI 리뷰 요약" "Phase 5: AI/RAG" "ai,feature" \
"음반별 리뷰 → LLM 요약(장점/단점), 배치 사전 생성."

echo ""
echo "==> Done. https://github.com/$REPO/milestones 및 /issues 확인하세요."
