package miiiiiin.com.vinyler.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import miiiiiin.com.vinyler.global.GlobalResponseDto;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginResponseDto implements GlobalResponseDto {
    private String accessToken;
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
