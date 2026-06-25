package miiiiiin.com.vinyler.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "공통 응답 래퍼")
public class DefaultResponseDto<T> {

    @Schema(description = "응답 데이터")
    private T data;

    public DefaultResponseDto(T data) {
        this.data = data;
    }
}
