package miiiiiin.com.vinyler.auth.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Getter
public class TokenInfoDto {
    private String grantType;
    private String accessToken;
    private String refreshToken;
}
