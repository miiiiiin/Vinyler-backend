package miiiiiin.com.vinyler.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Vinyl 찜 상태 응답")
public class VinylLikeDto {
    @Schema(description = "내부 Vinyl ID", example = "1")
    private Long vinylId;

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @JsonProperty("is_liking")
    @Schema(description = "현재 사용자가 찜한 상태인지 여부", example = "true")
    private boolean isLiking;

    public static VinylLikeDto from(Vinyl vinyl, User user, boolean isLiking) {
        return VinylLikeDto.builder()
                .vinylId(vinyl.getVinylId())
                .userId(user.getUserId())
                .isLiking(isLiking)
                .build();
    }
}
