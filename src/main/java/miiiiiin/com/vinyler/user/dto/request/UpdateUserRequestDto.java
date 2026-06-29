package miiiiiin.com.vinyler.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import miiiiiin.com.vinyler.user.enums.ProfileVisibility;

import java.time.LocalDate;

@Schema(description = "유저 정보 수정")
public record UpdateUserRequestDto(
        @Schema(description = "닉네임", example = "바이닐러") String nickname,
        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg") String profile,
        @Schema(description = "생년월일", example = "2005-06-15") LocalDate birthday,
        @Schema(description = "프로필 공개 범위", example = "PUBLIC") ProfileVisibility visibility
) {
}
