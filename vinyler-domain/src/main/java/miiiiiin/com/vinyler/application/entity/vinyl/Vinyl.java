package miiiiiin.com.vinyler.application.entity.vinyl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.user.entity.User;

import java.util.ArrayList;
import java.util.List;


/**
 * 사용자가 좋아요를 누르면 DB에 해당 Vinyl을 저장
 */

@Table(name = "vinyls", indexes = @Index(name = "vinyl_userid_idx", columnList = "user_id"))
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Vinyl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // DB 내부 관리하는 ID
    private Long vinylId;

    @Column(unique = true, nullable = false)
    // Discogs에서 받아온 Release ID (클라이언트에서 넘어온 값)
    private Long discogsId;

    @Column(nullable = true)
    private Long likesCount = 0L;

    @Column(nullable = true)
    private Long reviewsCount = 0L;

    @Column(nullable = true)
    private String title;

    @Column(name = "artists_sort")
    private String artistsSort;

    @Column(name = "released_formatted")
    private String releasedFormatted;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Like 테이블에서 Vinyl을 N:1 관계로 참조
     */
    @OneToMany(mappedBy = "vinyl", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore  // 직렬화 시 likes를 무시
    private List<Like> likes = new ArrayList<>();

}
