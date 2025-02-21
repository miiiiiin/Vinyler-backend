package miiiiiin.com.vinyler.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import miiiiiin.com.vinyler.application.entity.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LikeRequestDto {
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
}
