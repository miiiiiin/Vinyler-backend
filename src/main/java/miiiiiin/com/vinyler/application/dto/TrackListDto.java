package miiiiiin.com.vinyler.application.dto;

import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.TrackList;

@Builder
@Data
public class TrackListDto {
    private String title;
    private String duration;
    private String position;

    public static TrackListDto of(TrackList trackList) {
        return TrackListDto.builder()
                .title(trackList.getTitle())
                .duration(trackList.getDuration())
                .position(trackList.getPosition())
                .build();
    }
}