package miiiiiin.com.vinyler.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "로그인 요청")
public class LoginRequestDto {
    @NotEmpty
    @Schema(description = "이메일 주소", example = "user@example.com")
    String email;

    @NotEmpty
    @Schema(description = "비밀번호", example = "password123!")
    String password;
}
