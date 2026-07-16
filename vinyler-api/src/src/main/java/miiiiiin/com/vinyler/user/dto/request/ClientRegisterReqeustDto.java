package miiiiiin.com.vinyler.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "회원가입 요청")
public class ClientRegisterReqeustDto {
    @NotEmpty
    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    @NotEmpty
    @Schema(description = "비밀번호", example = "password123!")
    private String password;

    @NotEmpty
    @Schema(description = "닉네임", example = "바이닐러")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profile;

    @Schema(description = "생년월일", example = "1995-06-15")
    private LocalDate birthday;
}
