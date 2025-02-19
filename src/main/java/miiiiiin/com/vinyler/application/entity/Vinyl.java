package miiiiiin.com.vinyler.application.entity;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.*;
import lombok.*;
import miiiiiin.com.vinyler.user.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Table(name = "vinyls", indexes = @Index(name = "vinyl_userid_idx", columnList = "userid"))
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Vinyl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vinylId;

    @Column
    private Long likesCount = 0L;

    @Column(name = "artists_sort")
    private String artistsSort;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "released_formatted")
    private String releasedFormatted;

    @Column
    private String uri;

    @Column
    private String status;

    @OneToMany(mappedBy = "vinyl", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "vinyl", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrackList> tracklist = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "userid")
    private User user;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vinyl vinyl = (Vinyl) o;
        return Objects.equals(vinylId, vinyl.vinylId) && Objects.equals(likesCount, vinyl.likesCount) && Objects.equals(artistsSort, vinyl.artistsSort) && Objects.equals(notes, vinyl.notes) && Objects.equals(releasedFormatted, vinyl.releasedFormatted) && Objects.equals(uri, vinyl.uri) && Objects.equals(status, vinyl.status) && Objects.equals(images, vinyl.images) && Objects.equals(tracklist, vinyl.tracklist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vinylId, likesCount, artistsSort, notes, releasedFormatted, uri, status, images, tracklist);
    }

}
