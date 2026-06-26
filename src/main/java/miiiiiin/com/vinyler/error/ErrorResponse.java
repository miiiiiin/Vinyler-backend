package miiiiiin.com.vinyler.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "에러 응답")
public record ErrorResponse(
        @Schema(description = "HTTP 상태 코드", example = "400") HttpStatus status,
        @Schema(description = "에러 메시지", example = "유효하지 않은 요청입니다.") Object message) {
}