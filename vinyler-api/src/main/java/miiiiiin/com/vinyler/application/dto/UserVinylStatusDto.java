package miiiiiin.com.vinyler.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Vinyl 감상 상태 응답")
public class UserVinylStatusDto {
    @Schema(description = "내부 Vinyl ID", example = "1")
    private Long vinylId;

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "현재 사용자가 감상한 상태인지 여부", example = "true")
    private boolean listened;

    public static UserVinylStatusDto from(Vinyl vinyl, User user, boolean listened) {
        return UserVinylStatusDto.builder()
                .vinylId(vinyl.getVinylId())
                .userId(user.getUserId())
                .listened(listened)
                .build();
    }
}
