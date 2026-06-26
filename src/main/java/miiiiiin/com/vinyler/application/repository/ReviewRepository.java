package miiiiiin.com.vinyler.application.repository;

import miiiiiin.com.vinyler.application.entity.Review;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
//    Optional<Review> findByUserIdAndDiscogsId(Long userId, Long discogsId);
    Optional<Review> findByUserAndVinyl(User user, Vinyl vinyl);
    @Query("""
      SELECT r
      FROM Review r
      WHERE r.vinyl = :vinyl
        AND (:cursorId IS NULL OR r.id < :cursorId)
      ORDER BY r.id DESC
    """)
    List<Review> findByVinylWithCursor(Vinyl vinyl, @Param("cursorId") Long cursorId, Pageable pageable);
    List<Review> findByUser(User user);
    Vinyl vinyl(Vinyl vinyl);
}
