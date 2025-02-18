package miiiiiin.com.vinyler.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

// DTO
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(HttpStatus status, Object message) {

}