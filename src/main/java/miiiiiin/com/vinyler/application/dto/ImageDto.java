package miiiiiin.com.vinyler.application.dto;

import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.vinyl.Image;

@Data
@Builder
public class ImageDto {
    private String type;
    private String uri;

    public static ImageDto of(Image image) {
        return ImageDto.builder()
                .type(image.getType())
                .uri(image.getUri())
                .build();
    }
}
