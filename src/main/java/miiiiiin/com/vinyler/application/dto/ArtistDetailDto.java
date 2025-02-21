package miiiiiin.com.vinyler.application.dto;

import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.ArtistDetail;
import miiiiiin.com.vinyler.application.entity.Format;

@Data
@Builder
public class ArtistDetailDto {
    private String name;
    private String resourceUrl;

    public static ArtistDetailDto of(ArtistDetail artist) {
        return ArtistDetailDto.builder()
                .name(artist.getName())
                .resourceUrl(artist.getResourceUrl())
                .build();
    }
}
