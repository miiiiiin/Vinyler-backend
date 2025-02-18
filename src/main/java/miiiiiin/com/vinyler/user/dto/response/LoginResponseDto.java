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

    public static LoginResponseDto from(String accessToken) {
        return new LoginResponseDto(accessToken);
    }

    @Override
    public String message(String msg) {
        if (msg == null) { return ""; }
        return msg;
    }
}
