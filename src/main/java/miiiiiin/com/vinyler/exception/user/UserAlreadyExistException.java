package miiiiiin.com.vinyler.exception.user;

import miiiiiin.com.vinyler.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistException extends ClientErrorException{
    public UserAlreadyExistException() {
        super(HttpStatus.CONFLICT, "USER ALREADY EXISTS");
    }
    // 예외가 발생했을 때, 구체적인 email이나 nickname을 알고 있을 경우
    public UserAlreadyExistException(String str) {
        super(HttpStatus.CONFLICT, "USER : " + str + " ALREADY EXISTS");
    }
}
