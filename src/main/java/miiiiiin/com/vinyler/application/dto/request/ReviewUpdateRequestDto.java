package miiiiiin.com.vinyler.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewUpdateRequestDto {
    @NotNull
    private Long reviewId;
    @NotNull
    private Long discogsId;
    @Min(0)
    @Max(5)
    private int rating;
    @NotBlank
    private String content;
}
