package miiiiiin.com.vinyler.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VinylLikeDto {
    private Long vinylId;
    private Long userId;
    // 특정 사용자가 해당 Vinyl을 "좋아요" 했는지 여부
    @JsonProperty("is_liking")
    private boolean isLiking;

    public static VinylLikeDto from(Vinyl vinyl, User user, boolean isLiking) {
        return VinylLikeDto.builder()
                .vinylId(vinyl.getVinylId())
                .userId(user.getUserId())
                .isLiking(isLiking)
                .build();
    }
}
