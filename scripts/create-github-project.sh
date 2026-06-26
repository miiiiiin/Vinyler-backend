#!/usr/bin/env bash
#
# Vinyler 개발 로드맵 → GitHub Projects(v2) 보드 자동 생성 스크립트
#
# 사전 준비 (로컬 PC에서):
#   1) gh CLI 설치:        brew install gh        (https://cli.github.com)
#   2) jq 설치:            brew install jq
#   3) 로그인:             gh auth login
#   4) project 권한 추가:  gh auth refresh -s project,read:project
#   5) 실행:               bash scripts/create-github-project.sh
#
# 생성물:
#   - "Vinyler 개발 로드맵" Projects(v2) 보드 (본인 계정 소유)
#   - 커스텀 필드: Phase(단일선택), 시작일/마감일/종료일(날짜)
#   - Phase 0~5 모든 작업이 draft 카드로 추가되고 날짜/Phase가 채워짐
#
set -euo pipefail

OWNER="@me"                       # 조직 보드면 "조직명"으로 변경
TITLE="Vinyler 개발 로드맵"

command -v gh >/dev/null || { echo "❌ gh CLI 필요: brew install gh"; exit 1; }
command -v jq >/dev/null || { echo "❌ jq 필요: brew install jq"; exit 1; }

echo "▶ 프로젝트 생성: $TITLE"
PROJECT_JSON=$(gh project create --owner "$OWNER" --title "$TITLE" --format json)
PNUM=$(echo "$PROJECT_JSON" | jq -r '.number')
PID=$(echo "$PROJECT_JSON" | jq -r '.id')
PURL=$(echo "$PROJECT_JSON" | jq -r '.url')
echo "  ✔ #$PNUM  $PURL"

echo "▶ 필드 생성 (Phase, 시작일, 마감일, 종료일)"
gh project field-create "$PNUM" --owner "$OWNER" --name "시작일" --data-type DATE >/dev/null
gh project field-create "$PNUM" --owner "$OWNER" --name "마감일" --data-type DATE >/dev/null
gh project field-create "$PNUM" --owner "$OWNER" --name "종료일" --data-type DATE >/dev/null
gh project field-create "$PNUM" --owner "$OWNER" --name "Phase" --data-type SINGLE_SELECT \
  --single-select-options "Phase 0,Phase 1,Phase 2,Phase 3,Phase 4,Phase 5" >/dev/null

# 필드 ID/옵션 ID 조회
FIELDS=$(gh project field-list "$PNUM" --owner "$OWNER" --format json)
fid() { echo "$FIELDS" | jq -r --arg n "$1" '.fields[] | select(.name==$n) | .id'; }
START_F=$(fid "시작일"); DUE_F=$(fid "마감일"); END_F=$(fid "종료일"); PHASE_F=$(fid "Phase")
opt() { echo "$FIELDS" | jq -r --arg n "$1" '.fields[] | select(.name=="Phase") | .options[] | select(.name==$n) | .id'; }

add() { # add "Phase X" "제목" "시작일" "마감일"
  local phase="$1" title="$2" start="$3" due="$4"
  local item_id
  item_id=$(gh project item-create "$PNUM" --owner "$OWNER" --title "$title" --format json | jq -r '.id')
  [ -n "$start" ] && gh project item-edit --id "$item_id" --project-id "$PID" --field-id "$START_F" --date "$start" >/dev/null
  [ -n "$due" ]   && gh project item-edit --id "$item_id" --project-id "$PID" --field-id "$DUE_F"   --date "$due" >/dev/null
  gh project item-edit --id "$item_id" --project-id "$PID" --field-id "$PHASE_F" \
    --single-select-option-id "$(opt "$phase")" >/dev/null
  echo "  ✔ [$phase] $title"
}

echo "▶ 카드 추가"

# ── Phase 0 — 리팩토링 (6/26~6/30)
add "Phase 0" "버그: 팔로우 카운트 오류 수정"                 2026-06-26 2026-06-26
add "Phase 0" "버그: 팔로잉 목록 조회(findByFollower) 수정"   2026-06-26 2026-06-26
add "Phase 0" "버그: getVinylsListenedByUser TODO 처리"      2026-06-26 2026-06-26
add "Phase 0" "품질: @Transactional 추가"                    2026-06-26 2026-06-26
add "Phase 0" "품질: modifiedDate 중복 제거"                 2026-06-26 2026-06-26
add "Phase 0" "품질: User.equals() password 제외"           2026-06-26 2026-06-26
add "Phase 0" "품질: SliceResponse Raw type 제거"           2026-06-26 2026-06-26
add "Phase 0" "품질: UserController 불필요한 import 제거"     2026-06-26 2026-06-26
add "Phase 0" "리팩토링: Entity→DTO 의존 제거"               2026-06-28 2026-06-28
add "Phase 0" "접근 제어: 찜/감상 목록 본인·팔로워만"         2026-06-28 2026-06-28
add "Phase 0" "유저 정보 수정 PATCH /api/v1/user"           2026-06-29 2026-06-29
add "Phase 0" "유저 탈퇴 DELETE /api/v1/user"               2026-06-29 2026-06-29
add "Phase 0" "리뷰 삭제 DELETE /api/v1/reviews/{id}"        2026-06-29 2026-06-29
add "Phase 0" "감상 음반 목록 커서 페이징"                   2026-06-30 2026-06-30
add "Phase 0" "팔로워/팔로잉 커서 페이징"                    2026-06-30 2026-06-30
add "Phase 0" "Kakao OAuth2 활성화 여부 결정"               2026-06-30 2026-06-30
add "Phase 0" "전체 테스트 및 Swagger 확인"                 2026-06-30 2026-06-30

# ── Phase 1 — Search Layer (7/01~7/07)
add "Phase 1" "ES 환경 구성 (Docker + Nori)"                2026-07-01 2026-07-02
add "Phase 1" "Vinyl 색인 매핑 설계"                        2026-07-02 2026-07-03
add "Phase 1" "DB → ES 초기 색인"                           2026-07-03 2026-07-04
add "Phase 1" "검색 API (멀티필드 + fuzzy)"                 2026-07-04 2026-07-06
add "Phase 1" "자동완성 (edge n-gram)"                      2026-07-06 2026-07-07

# ── Phase 2 — Event Layer + 인기 집계 (7/08~7/14)
add "Phase 2" "Domain Event 정의 (좋아요/리뷰 등 2~3종)"     2026-07-08 2026-07-09
add "Phase 2" "Redis Stream 프로듀서/컨슈머 연동"            2026-07-09 2026-07-11
add "Phase 2" "멱등성(중복 소비 방지) 최소 처리"            2026-07-11 2026-07-12
add "Phase 2" "인기 LP 집계 (Redis Sorted Set) + API"      2026-07-12 2026-07-14

# ── Phase 3 — Batch Layer (7/15~7/21)
add "Phase 3" "vinyler-batch 프로젝트 분리 (Spring Batch)"   2026-07-15 2026-07-16
add "Phase 3" "리뷰 집계 배치 (rating 통계)"                2026-07-16 2026-07-18
add "Phase 3" "장르 트렌드 집계 배치"                       2026-07-18 2026-07-19
add "Phase 3" "ES 증분 색인 배치"                           2026-07-19 2026-07-20
add "Phase 3" "임베딩 색인 잡 (→Qdrant 적재, RAG 선반영)"    2026-07-20 2026-07-21

# ── Phase 4 — Notification (7/22~7/28)
add "Phase 4" "Discogs 가격 수집 배치"                      2026-07-22 2026-07-23
add "Phase 4" "가격 변동 감지 → 이벤트 발행"                2026-07-23 2026-07-25
add "Phase 4" "SSE 실시간 알림"                             2026-07-25 2026-07-27
add "Phase 4" "알림 이력 저장 + 조회 API"                   2026-07-27 2026-07-28

# ── Phase 5 — AI Layer / RAG (7/29~, 차차)
add "Phase 5" "Qdrant 구성 + 컬렉션 설계"                   2026-07-29 2026-07-30
add "Phase 5" "의미 검색 API (질문 임베딩→Vector Search)"   2026-07-30 2026-08-01
add "Phase 5" "RAG 추천 (Vector→GPT, ES 하이브리드)"        2026-08-01 ""
add "Phase 5" "AI 리뷰 요약 (배치 사전 생성)"               "" ""

echo ""
echo "✅ 완료!  보드 열기: $PURL"
echo "   팁: 보드에서 'Group by → Phase'로 보면 Phase별 칼럼으로 정리됩니다."
