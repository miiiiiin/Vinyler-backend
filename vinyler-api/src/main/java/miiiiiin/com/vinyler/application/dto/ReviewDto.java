package miiiiiin.com.vinyler.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.Review;

@Builder
@Data
@Schema(description = "리뷰 정보")
public class ReviewDto {
    @Schema(description = "리뷰 ID", example = "1")
    private Long id;

    @Schema(description = "평점 (0~5)", example = "4")
    private int rating;

    @Schema(description = "리뷰 내용", example = "명반입니다. 강추!")
    private String content;

    public static ReviewDto of(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .build();
    }
}
