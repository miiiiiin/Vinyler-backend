package miiiiiin.com.vinyler.application.entity;

import jakarta.persistence.*;
import lombok.*;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.user.entity.BaseEntity;
import miiiiiin.com.vinyler.user.entity.User;

import java.time.ZonedDateTime;

/**
 * 사전조건 : 음반을 감상했어요가 선행 조건
 * UniqueConstraint : 한 사용자가 같은 Vinyl에 대해 리뷰를 여러 개 남기지 못하도록 함
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "vinyl_id"}))
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int rating; // 별점 (1-5)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 리뷰 내용

    /**
     * "여러 리뷰"가 하나의 "user" 또는 "vinyl"에 연결
     * (N:1)
     * User와 Vinyl을 @ManyToOne 관계로 설정, 특정 사용자(User)가 특정 LP(Vinyl)에 리뷰를 남길 수 있도록 함.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "vinyl_id", nullable = false)
    private Vinyl vinyl;

    public static Review of(int rating, String content, User user, Vinyl vinyl) {
        return Review.builder()
                .rating(rating)
                .content(content)
                .user(user)
                .vinyl(vinyl)
                .build();
    }

    @PrePersist
    private void prePersist() {
        // 수정 시간을 생성 시간과 동일하게 맞춰줌
        setCreatedDate(ZonedDateTime.now());
        setModifiedDate(getCreatedDate());
    }

    @PreUpdate
    private void preUpdate() {
        // 업데이트가 발생된 시점을 현재 시점으로 기록
        setModifiedDate(ZonedDateTime.now());
    }
}


