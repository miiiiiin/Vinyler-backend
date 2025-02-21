package miiiiiin.com.vinyler.application.dto;

import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.ArtistDetail;
import miiiiiin.com.vinyler.application.entity.Video;

@Builder
@Data
public class VideoDto {
    private String uri;

    public static VideoDto of(Video video) {
        return VideoDto.builder()
                .uri(video.getUri())
                .build();
    }
}
