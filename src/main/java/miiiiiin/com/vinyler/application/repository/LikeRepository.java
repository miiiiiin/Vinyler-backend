package miiiiiin.com.vinyler.application.repository;

import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    @Query("SELECT l FROM Like l JOIN FETCH l.vinyl v WHERE l.user = :user")
    List<Like> findByUser(User user);
    List<Like> findByVinyl(Vinyl vinyl);
    Optional<Like> findByUserAndVinyl(User user, Vinyl vinyl);
}
