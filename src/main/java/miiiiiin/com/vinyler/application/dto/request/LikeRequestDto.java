package miiiiiin.com.vinyler.application.dto.request;

import java.util.List;
import miiiiiin.com.vinyler.application.entity.vinyl.ArtistDetail;
import miiiiiin.com.vinyler.application.entity.vinyl.Format;
import miiiiiin.com.vinyler.application.entity.vinyl.Image;
import miiiiiin.com.vinyler.application.entity.vinyl.TrackList;
import miiiiiin.com.vinyler.application.entity.vinyl.Video;

public record LikeRequestDto(Long discogsId,
                             String title,
                             String artistsSort,
                             String notes,
                             String status,
                             String uri,
                             String releasedFormatted,
                             List<TrackList> tracklist,
                             List<Image> images,
                             List<Format> formats,
                             List<Video> videos,
                             List<ArtistDetail> artists) {

}