package miiiiiin.com.vinyler.exception.user;

import miiiiiin.com.vinyler.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ClientErrorException {
    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "USER NOT FOUND");
    }

    // 예외가 발생했을 때, 구체적인 이름를 알고 있을 경우
    public UserNotFoundException(String nickname) {
        super(HttpStatus.NOT_FOUND, "USER with nickname " + nickname + " NOT FOUND");
    }
}
