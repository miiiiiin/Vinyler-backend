package miiiiiin.com.vinyler.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.Review;
import miiiiiin.com.vinyler.global.GlobalResponseDto;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@Schema(description = "리뷰 작성/수정 응답")
public class ReviewResponseDto implements GlobalResponseDto {
    @Schema(description = "리뷰 ID", example = "1")
    private Long reviewId;

    @Schema(description = "Discogs Release ID", example = "1234567")
    private Long discogsId;

    @Schema(description = "리뷰 작성/수정 일시")
    private LocalDateTime reviewedDate;

    public static ReviewResponseDto from(Review review) {
        return new ReviewResponseDto(review.getId(), review.getVinyl().getDiscogsId(), LocalDateTime.now());
    }

    @Override
    public String message(String msg) {
        if (msg == null) { return ""; }
        return msg;
    }
}
