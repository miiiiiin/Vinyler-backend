package miiiiiin.com.vinyler.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.vinyl.Image;

@Data
@Builder
@Schema(description = "음반 이미지 정보")
public class ImageDto {
    @Schema(description = "이미지 타입", example = "primary")
    private String type;

    @Schema(description = "이미지 URL", example = "https://i.discogs.com/example.jpg")
    private String uri;

    public static ImageDto of(Image image) {
        return ImageDto.builder()
                .type(image.getType())
                .uri(image.getUri())
                .build();
    }
}
