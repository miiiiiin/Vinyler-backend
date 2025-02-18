package miiiiiin.com.vinyler.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import miiiiiin.com.vinyler.global.GlobalResponseDto;
import miiiiiin.com.vinyler.user.entity.User;

//  JSON 응답을 생성할 때, 필드에 대한 getter가 필요: @Data 사용해서 getter 자동 생성
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto implements GlobalResponseDto {
    String nickname;
    String email;

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getNickname(),
                user.getEmail()
        );
    }

    @Override
    public String message(String msg) {
        if (msg == null) { return ""; }
        return msg;
    }
}
