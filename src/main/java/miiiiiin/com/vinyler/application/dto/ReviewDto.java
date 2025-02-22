package miiiiiin.com.vinyler.application.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ReviewDto {
    private int rating;
    private String content;

    public static ReviewDto of(int rating, String content) {
        return ReviewDto.builder()
                .rating(rating)
                .content(content)
                .build();
    }
}
