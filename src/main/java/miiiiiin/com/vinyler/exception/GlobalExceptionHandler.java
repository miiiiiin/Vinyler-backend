package miiiiiin.com.vinyler.exception;

import miiiiiin.com.vinyler.error.ClientErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<ClientErrorResponse> handleClientErrorException(ClientErrorException e) {
        return new ResponseEntity<>(new ClientErrorResponse(e.getStatus(), e.getMessage()), e.getStatus());
    }

    /**
     * 서버 내부 발생 에러 핸들링
     * 내부 에러 전달하는 대신 스테이터스 코드만 보내주는 걸로 간소화
     * @param
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ClientErrorResponse> handleClientErrorException(RuntimeException e) {
        return ResponseEntity.internalServerError().build();

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ClientErrorResponse> handleClientErrorException(Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}
