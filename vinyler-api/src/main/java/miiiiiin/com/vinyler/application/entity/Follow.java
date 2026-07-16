package miiiiiin.com.vinyler.application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.user.entity.BaseEntity;
import miiiiiin.com.vinyler.user.entity.User;
import net.minidev.json.annotate.JsonIgnore;

import java .time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "\"follow\"")
public class Follow extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 같은 userentity 내에서 follower와 following을 할 수 있음
     * 유저 : follower (여러 개 팔로우 가능)
     * 유저 : following (여러 개 팔로우 가능)
     * 팔로우는 (팔로워, 팔로잉)각각 1명의 유저에게 종속
     */
    @ManyToOne
    @JoinColumn(name = "follower")
    private User follower;

    @ManyToOne
    @JoinColumn(name = "following")
    private User following;

    @ManyToOne
    @JoinColumn(name = "vinyl_id")
    @JsonIgnore
    private Vinyl vinyl;

    public static Follow of(User follower, User following) {
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        return follow;
    }

    @PrePersist
    private void prePersist() {
        // Entity 처음 생성될 때, 현재 시간 기록. 생성했을 때에도
        // 수정 시간을 생성 시간과 동일하게 맞춰줌
        setCreatedDate(ZonedDateTime.now());
    }
}
