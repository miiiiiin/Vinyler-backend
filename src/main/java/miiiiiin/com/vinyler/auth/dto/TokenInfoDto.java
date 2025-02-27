package miiiiiin.com.vinyler.auth.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TokenInfoDto {
    private String grantType;
    private String accessToken;
    private String refreshToken;
}
