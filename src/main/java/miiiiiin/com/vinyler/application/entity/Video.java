package miiiiiin.com.vinyler.application.entity;

import jakarta.persistence.*;

@Entity
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vinyl_id")
    private Vinyl vinyl;
}
