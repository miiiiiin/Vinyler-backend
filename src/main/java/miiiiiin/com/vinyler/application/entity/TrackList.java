package miiiiiin.com.vinyler.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity(name = "tracklist")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TrackList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String duration;
    private String position;

    @ManyToOne
    @JoinColumn(name = "vinylid", nullable = false)
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
