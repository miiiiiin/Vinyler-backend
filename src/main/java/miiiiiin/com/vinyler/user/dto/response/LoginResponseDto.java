package miiiiiin.com.vinyler.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import miiiiiin.com.vinyler.global.GlobalResponseDto;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "로그인 응답")
public class LoginResponseDto implements GlobalResponseDto {
    @Schema(description = "Access Token (Authorization 헤더에 Bearer로 전달)", example = "eyJhbGci...")
    private String accessToken;

    @Schema(description = "Refresh Token (HttpOnly 쿠키로 전달)", example = "eyJhbGci...")
    private String refreshToken;

    public static LoginResponseDto from(String accessToken, String refreshToken) {
        return new LoginResponseDto(accessToken, refreshToken);
    }

    @Override
    public String message(String msg) {
        if (msg == null) { return ""; }
        return msg;
    }
}
