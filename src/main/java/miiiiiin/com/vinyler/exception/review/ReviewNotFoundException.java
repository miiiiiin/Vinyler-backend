package miiiiiin.com.vinyler.exception.review;

import miiiiiin.com.vinyler.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ReviewNotFoundException extends ClientErrorException {
    public ReviewNotFoundException() {
        super(HttpStatus.NOT_FOUND, "REVIEW NOT FOUND");
    }

    // 예외가 발생했을 때, 구체적인 이름를 알고 있을 경우
    public ReviewNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "REVIEW with id " + id + " NOT FOUND");
    }
}
