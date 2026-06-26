package miiiiiin.com.vinyler.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.vinyl.TrackList;

@Builder
@Data
@Schema(description = "트랙 정보")
public class TrackListDto {
    @Schema(description = "트랙 제목", example = "So What")
    private String title;

    @Schema(description = "재생 시간", example = "9:22")
    private String duration;

    @Schema(description = "트랙 번호", example = "A1")
    private String position;

    public static TrackListDto of(TrackList trackList) {
        return TrackListDto.builder()
                .title(trackList.getTitle())
                .duration(trackList.getDuration())
                .position(trackList.getPosition())
                .build();
    }
}