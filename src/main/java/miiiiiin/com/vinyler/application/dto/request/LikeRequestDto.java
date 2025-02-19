package miiiiiin.com.vinyler.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.application.entity.Image;
import miiiiiin.com.vinyler.application.entity.TrackList;
import miiiiiin.com.vinyler.application.entity.Vinyl;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LikeRequestDto {
    private Long vinylId;
    private String artistsSort;
    private String notes;
    private String status;
    private String uri;
    private String releasedFormatted;
    private List<TrackList> tracklist;
    private List<Image> images;

    public Vinyl toEntity() {
        return Vinyl.builder()
                .vinylId(vinylId)
                .artistsSort(artistsSort)
                .status(status)
                .uri(uri)
                .notes(notes)
                .releasedFormatted(releasedFormatted)
                .tracklist(tracklist)
                .images(images)
                .build();
    }
}
