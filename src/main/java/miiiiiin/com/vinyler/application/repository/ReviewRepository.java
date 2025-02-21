package miiiiiin.com.vinyler.application.repository;

import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.application.entity.Review;
import miiiiiin.com.vinyler.application.entity.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserAndVinyl(User user, Vinyl vinyl);
//    Optional<Review> findByUserIdAndVinylId(Long userId, Long vinylId);
    List<Review> findByVinylId(Long vinylId);
}
