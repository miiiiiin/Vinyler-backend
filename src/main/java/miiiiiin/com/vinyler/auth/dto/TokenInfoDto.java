package miiiiiin.com.vinyler.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(description = "토큰 정보")
public class TokenInfoDto {
    @Schema(description = "인증 타입", example = "Bearer")
    private String grantType;

    @Schema(description = "Access Token", example = "eyJhbGci...")
    private String accessToken;

    @Schema(description = "Refresh Token", example = "eyJhbGci...")
    private String refreshToken;
}
