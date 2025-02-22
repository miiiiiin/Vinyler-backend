package miiiiiin.com.vinyler.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDto {
    @NotNull
    private Long discogsId;
    @Min(0)
    @Max(5)
    private int rating;
    @NotBlank
    private String content;
}
