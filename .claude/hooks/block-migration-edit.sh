#!/bin/bash
# 이미 적용된 Flyway 마이그레이션을 수정하면 체크섬이 깨져
# 다른 환경(팀원 로컬/CI/운영)의 배포가 실패한다.
# 새 버전 파일 추가는 허용하고, 기존 파일 수정만 차단한다.

input=$(cat)
file_path=$(printf '%s' "$input" | jq -r '.tool_input.file_path // empty')

[ -z "$file_path" ] && exit 0

case "$file_path" in
  */db/migration/*|db/migration/*) ;;
  *) exit 0 ;;
esac

# 아직 없는 파일 = 새 마이그레이션 추가 → 허용
[ -e "$file_path" ] || exit 0

cat >&2 <<EOF
기존 Flyway 마이그레이션 파일은 수정할 수 없습니다: $(basename "$file_path")

이미 적용된 마이그레이션을 고치면 체크섬이 깨져 다른 환경의 배포가 실패합니다.
스키마를 바꾸려면 새 버전 파일(V{n}__{설명}.sql)을 추가하세요.
EOF
exit 2
