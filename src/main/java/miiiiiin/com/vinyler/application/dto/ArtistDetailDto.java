package miiiiiin.com.vinyler.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.vinyl.ArtistDetail;

@Data
@Builder
@Schema(description = "아티스트 정보")
public class ArtistDetailDto {
    @Schema(description = "아티스트 이름", example = "Miles Davis")
    private String name;

    @Schema(description = "Discogs 아티스트 리소스 URL", example = "https://api.discogs.com/artists/1234")
    private String resourceUrl;

    @Schema(description = "활동 중 여부", example = "true")
    private Boolean active;

    public static ArtistDetailDto of(ArtistDetail artist) {
        return ArtistDetailDto.builder()
                .name(artist.getName())
                .resourceUrl(artist.getResourceUrl())
                .active(artist.getActive())
                .build();
    }
}
