package miiiiiin.com.vinyler.exception.review;

import miiiiiin.com.vinyler.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ReviewNotAvailableException extends ClientErrorException {
    // 유저가 리뷰를 작성할 권한이 없음 (음반 감상 여부를 표시하지 않았을 때)
    public ReviewNotAvailableException() {
        super(HttpStatus.FORBIDDEN, "Review not available");
    }

    // TODO: FIX 앨범 이름 표시
    public ReviewNotAvailableException(String name) {
        super(HttpStatus.FORBIDDEN, "Review with vinyl name " + name + " NOT AVAILABLE");
    }
}
