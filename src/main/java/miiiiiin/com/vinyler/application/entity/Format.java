package miiiiiin.com.vinyler.application.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Format {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ElementCollection
    @CollectionTable(name = "format_descriptions", joinColumns = @JoinColumn(name = "format_id"))
    @Column(name = "description")
    private List<String> descriptions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vinyl_id")
    private Vinyl vinyl;
}
