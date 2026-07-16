package miiiiiin.com.vinyler.exception.discogs;

import miiiiiin.com.vinyler.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class DiscogsUnavailableException extends ClientErrorException {
    /**
     * RestClient의 .retrieve()는 4xx/5xx 일때 던지는 예외
     * ClientErrorException을 상속했으니 GlobalExceptionHandler가 자동으로 503 + 메시지로 응답해줌
     * 500 이면 우리 서버 버그이나, 503(Service Unavailable)은 Discogs가 잠깐 죽거나 rate limit일 때 적합한 에러 코드
     * 클라이언트 쪽에서 "잠시 후 다시" ux 처리
     */
    public DiscogsUnavailableException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "DISCOGS TEMPORARILY UNAVAILABLE");
    }
}
