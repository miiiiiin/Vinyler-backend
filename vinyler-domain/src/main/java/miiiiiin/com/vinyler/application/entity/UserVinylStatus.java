package miiiiiin.com.vinyler.application.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;

/**
 * 감상 여부를 저장
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "user_vinyl_status")
public class UserVinylStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "vinyl_id", nullable = false)
    private Vinyl vinyl;

    /**
     * 감상 여부 확인 -> 기본값 false
     */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private boolean listened;

    /**
     * 찜 여부 확인 -> 기본값 false
     */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private boolean liked;


    public static UserVinylStatus of(User user, Vinyl vinyl, boolean listened) {
        var status = new UserVinylStatus();
        status.setUser(user);
        status.setVinyl(vinyl);
        status.setListened(listened);
        return status;
    }

    // 엔티티가 처음 persist될 때 listened 값을 false로 설정
//    @PrePersist
//    public void prePersist() {
//        if (this.listened == null) {
//            this.listened = false;  // 기본값 false
//        }
//    }
}
