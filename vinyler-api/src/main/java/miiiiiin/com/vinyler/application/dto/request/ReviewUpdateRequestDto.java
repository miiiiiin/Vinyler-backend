package miiiiiin.com.vinyler.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "리뷰 수정 요청")
public class ReviewUpdateRequestDto {
    @NotNull
    @Schema(description = "수정할 리뷰 ID", example = "1")
    private Long reviewId;

    @NotNull
    @Schema(description = "Discogs Release ID", example = "1234567")
    private Long discogsId;

    @Min(0)
    @Max(5)
    @Schema(description = "평점 (0~5)", example = "5")
    private int rating;

    @NotBlank
    @Schema(description = "리뷰 내용", example = "다시 들어봤는데 더 좋네요!")
    private String content;
}
