package miiiiiin.com.vinyler.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import miiiiiin.com.vinyler.application.entity.vinyl.ArtistDetail;
import miiiiiin.com.vinyler.application.entity.vinyl.Format;
import miiiiiin.com.vinyler.application.entity.vinyl.Image;
import miiiiiin.com.vinyler.application.entity.vinyl.TrackList;
import miiiiiin.com.vinyler.application.entity.vinyl.Video;

public record LikeRequestDto(
    @JsonProperty("discogs_id")
    Long discogsId,
    String title,
    @JsonProperty("artists_sort")
    String artistsSort,
    String notes,
    String status,
    String uri,
    @JsonProperty("released_formatted")
    String releasedFormatted,
    List<TrackList> tracklist,
    List<Image> images,
    List<Format> formats,
    List<Video> videos,
    List<ArtistDetail> artists) {
}