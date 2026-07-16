package miiiiiin.com.vinyler.exception.vinyl;

import miiiiiin.com.vinyler.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class VinylNotFoundException extends ClientErrorException {
    public VinylNotFoundException() {
        super(HttpStatus.NOT_FOUND, "VINYL NOT FOUND");
    }

    // 예외가 발생했을 때, 구체적인 이름를 알고 있을 경우
    public VinylNotFoundException(Long vinylId) {
        super(HttpStatus.NOT_FOUND, "VINYL with id " + vinylId + " NOT FOUND");
    }
}
