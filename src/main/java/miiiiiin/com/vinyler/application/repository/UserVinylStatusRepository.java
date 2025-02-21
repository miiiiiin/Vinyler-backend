package miiiiiin.com.vinyler.application.repository;

import miiiiiin.com.vinyler.application.entity.UserVinylStatus;
import miiiiiin.com.vinyler.application.entity.Vinyl;
import miiiiiin.com.vinyler.application.service.VinylService;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserVinylStatusRepository extends JpaRepository<UserVinylStatus, Long> {
    // 특정 사용자가 특정 LP의 상태를 조회
    UserVinylStatus findByUserAndVinyl(User user, Vinyl vinyl);
    // 특정 사용자가 감상한 LP들을 조회
    List<UserVinylStatus> findByUserAndListened(User user, boolean listened);
}
