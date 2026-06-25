package miiiiiin.com.vinyler.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.vinyl.Video;

@Builder
@Data
@Schema(description = "비디오 정보")
public class VideoDto {
    @Schema(description = "비디오 URL", example = "https://www.youtube.com/watch?v=example")
    private String uri;

    public static VideoDto of(Video video) {
        return VideoDto.builder()
                .uri(video.getUri())
                .build();
    }
}
