package miiiiiin.com.vinyler.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import miiiiiin.com.vinyler.application.entity.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserVinylStatusDto {
    private Long vinylId;
    private Long userId;
    // 특정 사용자가 해당 Vinyl을 "감상" 처리 했는지 여부
    private boolean listened;

    public static UserVinylStatusDto from(Vinyl vinyl, User user, boolean listened) {
        return UserVinylStatusDto.builder()
                .vinylId(vinyl.getVinylId())
                .userId(user.getUserId())
                .listened(listened)
                .build();
    }
}
