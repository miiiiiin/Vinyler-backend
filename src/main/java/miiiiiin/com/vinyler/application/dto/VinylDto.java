package miiiiiin.com.vinyler.application.dto;

import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.*;

import java.util.List;

@Builder
@Data
public class VinylDto {
    private Long discogsId;
    private String artistsSort;
    private String notes;
    private String status;
    private String uri;
    private String releasedFormatted;
    private List<TrackList> tracklist;
    private List<Image> images;
    private List<Format> formats;
    private List<Video> videos;
    private List<ArtistDetail> artists;

    public static VinylDto of(Vinyl vinyl) {
        return VinylDto.builder()
                .discogsId(vinyl.getDiscogsId())
                .artistsSort(vinyl.getArtistsSort())
                .status(vinyl.getStatus())
                .uri(vinyl.getUri())
                .notes(vinyl.getNotes())
                .releasedFormatted(vinyl.getReleasedFormatted())
                .tracklist(vinyl.getTracklist())
                .images(vinyl.getImages())
                .formats(vinyl.getFormats())
                .videos(vinyl.getVideos())
                .artists(vinyl.getArtists())
                .build();
    }
}
