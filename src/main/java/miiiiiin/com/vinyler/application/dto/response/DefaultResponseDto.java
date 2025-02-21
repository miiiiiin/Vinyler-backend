package miiiiiin.com.vinyler.application.dto.response;

import lombok.Data;

@Data
public class DefaultResponseDto<T> {

    private T data;

    public DefaultResponseDto(T data) {
        this.data = data;
    }
}
