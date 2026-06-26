package miiiiiin.com.vinyler.exception.review;

import miiiiiin.com.vinyler.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ReviewAlreadyExistException extends ClientErrorException{
    public ReviewAlreadyExistException() {
        super(HttpStatus.CONFLICT, "REVIEW ALREADY EXISTS");
    }
    public ReviewAlreadyExistException(String str) {
        super(HttpStatus.CONFLICT, "REVIEW : " + str + " ALREADY EXISTS");
    }
}
