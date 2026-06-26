package miiiiiin.com.vinyler.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.*;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;

import java.util.List;

@Builder
@Data
@Schema(description = "Vinyl(LP 음반) 정보")
public class VinylDto {
    @Schema(description = "내부 Vinyl ID", example = "1")
    private Long vinylId;

    @Schema(description = "Discogs Release ID", example = "1234567")
    private Long discogsId;

    @Schema(description = "음반 제목", example = "Kind of Blue")
    private String title;

    @Schema(description = "아티스트 이름", example = "Miles Davis")
    private String artistsSort;

    @Schema(description = "찜 수", example = "128")
    private Long likesCount;

    @Schema(description = "음반 노트")
    private String notes;

    @Schema(description = "음반 상태", example = "Accepted")
    private String status;

    @Schema(description = "Discogs URI", example = "https://www.discogs.com/release/1234567")
    private String uri;

    @Schema(description = "출시일 (포맷 문자열)", example = "1959")
    private String releasedFormatted;

    @Schema(description = "트랙 목록")
    private List<TrackListDto> tracklist;

    @Schema(description = "이미지 목록")
    private List<ImageDto> images;

    @Schema(description = "포맷 목록")
    private List<FormatDto> formats;

    @Schema(description = "비디오 목록")
    private List<VideoDto> videos;

    @Schema(description = "아티스트 목록")
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
            .vinylId(vinyl.getVinylId())
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
