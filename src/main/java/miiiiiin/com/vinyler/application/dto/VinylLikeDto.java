package miiiiiin.com.vinyler.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import miiiiiin.com.vinyler.application.entity.Vinyl;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VinylLikeDto {
    private Long vinylId;
    private Long userId;
    // 특정 사용자가 해당 Vinyl을 "좋아요" 했는지 여부
    private boolean isLiking;

    public static VinylLikeDto from(Vinyl vinyl, boolean isLiking) {
        return VinylLikeDto.builder()
                .vinylId(vinyl.getVinylId())
                .userId(vinyl.getUser().getUserId())
                .isLiking(isLiking)
                .build();
    }
}
