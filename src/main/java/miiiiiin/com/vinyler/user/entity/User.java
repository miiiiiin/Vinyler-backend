package miiiiiin.com.vinyler.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;

@Table(name = "\"user\"", indexes = {@Index(name = "user_username_idx", columnList = "email", unique = true)}) // username에도 인덱스 처리, db 레벨에서도 중복 생성 원천봉쇄
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@SQLDelete(sql = "UPDATE user SET deleted_date = CURRENT_TIMESTAMP WHERE userid = ?") // soft delete
@Where(clause = "deleted_date IS NULL")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = true)
    private ZonedDateTime deletedDate;

    @Column(nullable = true)
    private String profile; // profile 사진 url

    @Column(nullable = true)
    private LocalDate birthday;

    @Column
    private Long followersCount = 0L;

    @Column
    private Long followingsCount = 0L;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) && Objects.equals(email, user.email) && Objects.equals(password, user.password) && Objects.equals(nickname, user.nickname) && Objects.equals(deletedDate, user.deletedDate) && Objects.equals(profile, user.profile) && Objects.equals(birthday, user.birthday) && Objects.equals(followersCount, user.followersCount) && Objects.equals(followingsCount, user.followingsCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email, password, nickname, deletedDate, profile, birthday, followersCount, followingsCount);
    }

    public static User of(String email, String password, String nickname, String profile, LocalDate birthday) {
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .profile(profile)
                .birthday(birthday)
                .build();
    }

    /**
     * 엔티티가 처음 저장되기 직전에 실행되므로 기본값 확실히 보장
     */
    @PrePersist
    private void prePersist() {
        // 수정 시간을 생성 시간과 동일하게 맞춰줌
        setCreatedDate(ZonedDateTime.now());
        setModifiedDate(getCreatedDate());

        this.followersCount = this.followersCount == null ? 0L : this.followersCount;
        this.followingsCount = this.followingsCount == null ? 0L : this.followingsCount;
    }

    @PreUpdate
    private void preUpdate() {
        // 업데이트가 발생된 시점을 현재 시점으로 기록
        setModifiedDate(ZonedDateTime.now());
    }
}
