package miiiiiin.com.vinyler.application.entity.vinyl;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "images")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
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
