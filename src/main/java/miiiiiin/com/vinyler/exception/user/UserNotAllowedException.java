package miiiiiin.com.vinyler.exception.user;

import miiiiiin.com.vinyler.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

// 사용자가 발견되지 않았을 때 던져주는 예외
public class UserNotAllowedException extends ClientErrorException {
    public UserNotAllowedException() {
        super(HttpStatus.FORBIDDEN, "USER NOT ALLOWED");
    }
}
