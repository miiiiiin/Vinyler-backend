package miiiiiin.com.vinyler.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Table(name = "images")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private String type;
    private String uri;

    @ManyToOne
    @JoinColumn(name = "vinyl_id", nullable = false)
    private Vinyl vinyl;
}
