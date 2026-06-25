package miiiiiin.com.vinyler.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "리뷰 작성/수정 요청")
public class ReviewRequestDto {
    @NotNull
    @JsonProperty("discogs_id")
    @Schema(description = "Discogs Release ID", example = "1234567")
    private Long discogsId;

    @Min(0)
    @Max(5)
    @Schema(description = "평점 (0~5)", example = "4")
    private int rating;

    @Schema(description = "리뷰 내용", example = "명반입니다. 강추!")
    private String content;
}
