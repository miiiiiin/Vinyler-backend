package miiiiiin.com.vinyler.application.repository;

import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.application.entity.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findByUser(User user);
    List<Like> findByVinyl(Vinyl vinyl);
    Optional<Like> findByUserAndVinyl(User user, Vinyl vinyl);
}
