package miiiiiin.com.vinyler.application.dto.request;

import lombok.Getter;
import lombok.Setter;
import miiiiiin.com.vinyler.application.entity.vinyl.*;

import java.util.List;

@Getter
@Setter
public class LikeRequestDto {
    private AlbumInfo albumInfo;

    public record AlbumInfo(
        Long discogsId,
        String artistsSort,
        String notes,
        String status,
        String uri,
        String releasedFormatted,
        List<TrackList> tracklist,
        List<Image> images,
        List<Format> formats,
        List<Video> videos,
        List<ArtistDetail> artists
    ) { }
}
