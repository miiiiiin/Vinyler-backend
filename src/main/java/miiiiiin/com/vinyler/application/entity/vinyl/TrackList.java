package miiiiiin.com.vinyler.application.entity.vinyl;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity(name = "tracklist")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TrackList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String duration;
    private String position;

    @ManyToOne
    @JoinColumn(name = "vinyl_id", nullable = false)
    private Vinyl vinyl;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TrackList trackList = (TrackList) o;
        return Objects.equals(id, trackList.id) && Objects.equals(title, trackList.title) && Objects.equals(duration, trackList.duration) && Objects.equals(position, trackList.position) && Objects.equals(vinyl, trackList.vinyl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, duration, position, vinyl);
    }
}
