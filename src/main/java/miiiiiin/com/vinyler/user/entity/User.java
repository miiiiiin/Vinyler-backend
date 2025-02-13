package miiiiiin.com.vinyler.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Objects;

@Entity(name="member")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
//@SQLDelete(sql = "UPDATE member SET deletedDate = CURRENT_TIMESTAMP WHERE userid = ?") // soft delete
//@SQLRestriction("deletedDate IS NULL")
public class User extends BaseEntity {//implements UserDetails {
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

//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return null;
//    }
//
//    @Override
//    public String getUsername() {
//        return email;
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }

//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) && Objects.equals(email, user.email) && Objects.equals(password, user.password) && Objects.equals(nickname, user.nickname) && Objects.equals(deletedDate, user.deletedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email, password, nickname, deletedDate);
    }

    public static User of(String email, String password, String nickname) {
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
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
