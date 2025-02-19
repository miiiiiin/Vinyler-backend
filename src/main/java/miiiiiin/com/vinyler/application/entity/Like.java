package miiiiiin.com.vinyler.application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import miiiiiin.com.vinyler.user.entity.BaseEntity;
import miiiiiin.com.vinyler.user.entity.User;

import java.time.ZonedDateTime;
import java.util.Objects;

// unique true로 설정해서 중복 생성 방지
@Table(name = "\"like\"",
indexes = {@Index(name = "like_userid_vinylid_idx", columnList = "userid, vinylid", unique = true)})
@Entity
@Getter
@Setter
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
    @JoinColumn(name = "userid")
    private User user;

    /**
     * Vinyl 정보와 좋아요 관계 설정
     * (1:N)
     */
    @ManyToOne
    @JoinColumn(name = "vinylid")
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
