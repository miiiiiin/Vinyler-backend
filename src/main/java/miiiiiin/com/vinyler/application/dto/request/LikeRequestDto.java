package miiiiiin.com.vinyler.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import miiiiiin.com.vinyler.application.entity.vinyl.ArtistDetail;
import miiiiiin.com.vinyler.application.entity.vinyl.Format;
import miiiiiin.com.vinyler.application.entity.vinyl.Image;
import miiiiiin.com.vinyler.application.entity.vinyl.TrackList;
import miiiiiin.com.vinyler.application.entity.vinyl.Video;

@Schema(description = "Vinyl 찜/감상 요청 (Discogs 음반 정보 포함)")
public record LikeRequestDto(
    @JsonProperty("discogs_id")
    @Schema(description = "Discogs Release ID", example = "1234567")
    Long discogsId,

    @Schema(description = "음반 제목", example = "Kind of Blue")
    String title,

    @JsonProperty("artists_sort")
    @Schema(description = "아티스트 이름", example = "Miles Davis")
    String artistsSort,

    @Schema(description = "음반 노트")
    String notes,

    @Schema(description = "음반 상태", example = "Accepted")
    String status,

    @Schema(description = "Discogs URI", example = "https://www.discogs.com/release/1234567")
    String uri,

    @JsonProperty("released_formatted")
    @Schema(description = "출시일 (포맷 문자열)", example = "1959")
    String releasedFormatted,

    @Schema(description = "트랙 목록")
    List<TrackList> tracklist,

    @Schema(description = "이미지 목록")
    List<Image> images,

    @Schema(description = "포맷 목록")
    List<Format> formats,

    @Schema(description = "비디오 목록")
    List<Video> videos,

    @Schema(description = "아티스트 목록")
    List<ArtistDetail> artists) {
}
