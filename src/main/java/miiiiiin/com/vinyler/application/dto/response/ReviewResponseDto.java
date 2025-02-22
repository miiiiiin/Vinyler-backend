package miiiiiin.com.vinyler.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import miiiiiin.com.vinyler.application.dto.ReviewDto;
import miiiiiin.com.vinyler.application.entity.Review;
import miiiiiin.com.vinyler.application.entity.UserVinylStatus;
import miiiiiin.com.vinyler.global.GlobalResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class ReviewResponseDto implements GlobalResponseDto {
    private Long discogsId;
    private LocalDateTime reviewedDate;

    public static ReviewResponseDto from(Review review) {
        return new ReviewResponseDto(review.getVinyl().getDiscogsId(), LocalDateTime.now());
    }

    @Override
    public String message(String msg) {
        if (msg == null) { return ""; }
        return msg;
    }
}
