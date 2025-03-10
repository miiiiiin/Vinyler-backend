package miiiiiin.com.vinyler.application.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.user.entity.BaseEntity;
import miiiiiin.com.vinyler.user.entity.User;
import net.minidev.json.annotate.JsonIgnore;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Like는 User와 Vinyl을 각각 N:1 관계
 * 사용자가 좋아요를 누를 때 Vinyl이 DB에 없으면 먼저 저장 후 Like 생성하는 방식
 */
// unique true로 설정해서 중복 생성 방지
@Table(name = "\"like\"",
indexes = {@Index(name = "like_userid_vinylid_idx", columnList = "userid, vinylid", unique = true)})
@Entity
@Getter
@Setter
@JsonIgnoreProperties({"vinyl"}) // Like 직렬화 시 vinyl 필드 제외
public class Like extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    /**
     * 좋아요와 유저의 관계는 N:1
     * JOIN 컬럼으로 User Entity의 프라이머리 키 값 "userid"로 설정
     * 실제 테이블에서는 "userid"라는 컬럼으로 추가됨. 실제 데이터 관점에서 db에 저장될 때에는
     * "userid"기반으로 연동되나 실제 코드 작성 시에는 user만 사용해도 내부적으로 userid만으로 user를 가져와서 세팅 가능
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 좋아요와 Vinyl 관계 설정
     * (N:1)
     */
    @ManyToOne
    @JoinColumn(name = "vinyl_id", nullable = false)
    @JsonIgnore  // Vinyl 객체 직렬화 시 무시
    // DB에 저장된 Vinyl 참조
    private Vinyl vinyl;

    public static Like of(User user, Vinyl vinyl) {
        var like = new Like();
        like.setUser(user);
        like.setVinyl(vinyl);
        return like;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Like like = (Like) o;
        return Objects.equals(likeId, like.likeId) && Objects.equals(user, like.user) && Objects.equals(vinyl, like.vinyl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(likeId, user, vinyl);
    }

    /**
     * like Entity 처음 생성될 때, 현재 시간 기록. 생성했을 때에도
     * 수정 시간을 생성 시간과 동일하게 맞춰줌
     */
    @PrePersist
    private void prePersist() {
        setCreatedDate(ZonedDateTime.now());
    }
}
