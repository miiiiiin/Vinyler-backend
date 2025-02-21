package miiiiiin.com.vinyler.exception;

import miiiiiin.com.vinyler.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleClientErrorException(ClientErrorException e) {
        return new ResponseEntity<>(new ErrorResponse(e.getStatus(), e.getMessage()), e.getStatus());
    }

    /**
     * 서버 내부 발생 에러 핸들링
     * 내부 에러 전달하는 대신 스테이터스 코드만 보내주는 걸로 간소화
     * @param
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleClientErrorException(RuntimeException e) {
        return ResponseEntity.internalServerError().build();

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleClientErrorException(Exception e) {
        return ResponseEntity.internalServerError().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        var errorMessage = e.getFieldErrors().stream()
                .map(fieldError -> (fieldError.getField() + ": " + fieldError.getDefaultMessage()))
                .toList()
                .toString();
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST, errorMessage), HttpStatus.BAD_REQUEST);

    }

    /*
    REQUEST BODY가 없는 경우
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(MethodArgumentNotValidException e) {
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);

    }
}
