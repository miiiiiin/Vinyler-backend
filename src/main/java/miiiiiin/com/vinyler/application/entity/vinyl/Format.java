package miiiiiin.com.vinyler.application.entity.vinyl;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
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
    @JoinColumn(name = "vinyl_id", nullable = false)
    private Vinyl vinyl;
}
