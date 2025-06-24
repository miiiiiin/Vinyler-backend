package miiiiiin.com.vinyler.application.dto;

import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.*;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;

import java.util.List;

@Builder
@Data
public class VinylDto {
    private Long vinylId;
    private Long discogsId;
    private String title;
    private String artistsSort;
    private Long likesCount;
    private String notes;
    private String status;
    private String uri;
    private String releasedFormatted;
    private List<TrackListDto> tracklist;
    private List<ImageDto> images;
    private List<FormatDto> formats;
    private List<VideoDto> videos;
    private List<ArtistDetailDto> artists;

    public static VinylDto of(Like like) {
        Vinyl vinyl = like.getVinyl();
        return getVinylDto(vinyl);
    }

    public static VinylDto of(UserVinylStatus status) {
        Vinyl vinyl = status.getVinyl();
        return getVinylDto(vinyl);
    }

    public static VinylDto of(Vinyl vinyl) {
        return getVinylDto(vinyl);
    }

    private static VinylDto getVinylDto(Vinyl vinyl) {
        return VinylDto.builder()
            .discogsId(vinyl.getDiscogsId())
            .artistsSort(vinyl.getArtistsSort())
            .status(vinyl.getStatus())
            .uri(vinyl.getUri())
            .notes(vinyl.getNotes())
            .releasedFormatted(vinyl.getReleasedFormatted())
            .tracklist(vinyl.getTracklist().stream().map(TrackListDto::of).toList())
            .images(vinyl.getImages().stream().map(ImageDto::of).toList())
            .formats(vinyl.getFormats().stream().map(FormatDto::of).toList())
            .videos(vinyl.getVideos().stream().map(VideoDto::of).toList())
            .artists(vinyl.getArtists().stream().map(ArtistDetailDto::of).toList())
            .build();
    }
}
