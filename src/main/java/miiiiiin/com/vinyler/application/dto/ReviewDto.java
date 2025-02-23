package miiiiiin.com.vinyler.application.dto;

import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.Review;

@Builder
@Data
public class ReviewDto {
    private int id;
    private int rating;
    private String content;

    public static ReviewDto of(Review review) {
        return ReviewDto.builder()
                .id(review.getRating())
                .rating(review.getRating())
                .content(review.getContent())
                .build();
    }
}
