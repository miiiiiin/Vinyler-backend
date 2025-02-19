package miiiiiin.com.vinyler.application.entity;

import jakarta.persistence.*;

@Entity
public class ArtistDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String resourceUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vinyl_id")
    private Vinyl vinyl;
}
