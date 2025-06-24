package miiiiiin.com.vinyler.application.repository;

import io.lettuce.core.dynamic.annotation.Param;
import miiiiiin.com.vinyler.application.dto.projection.LikeVinylProjection;
import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    /**
     * JOIN FETCH는 연관된 Vinyl을 즉시 로딩하지만
     * Vinyl 내부에 @OneToMany 관계가 여러 개 (videos, images, likes, ...) 있다면,
     * 쿼리 결과가 Like 기준으로 중복 발생 → 페이징 쿼리가 정확하지 않음 (예: 중복 때문에 page size보다 적거나 많아짐)
     */
//    @Query("SELECT l FROM Like l JOIN FETCH l.vinyl v WHERE l.user = :user")
//    Slice<Like> findByUser(User user, Pageable pageable);
//    @EntityGraph(attributePaths = {"vinyl"})
//    @Query("""
//    SELECT l FROM Like l
//    WHERE l.user = :user
//      AND (:cursorId IS NULL OR l.id < :cursorId)
//    ORDER BY l.id DESC
//    """)


    @Query("""
      SELECT
          v.vinylId as vinylId,
          v.discogsId as discogsId,
          v.title as title,
          v.artistsSort as artistsSort,
          v.likesCount as likesCount,
          v.notes as notes,
          v.status as status,
          v.uri as uri,
          v.releasedFormatted as releasedFormatted
       FROM Like l
       JOIN l.vinyl v
       WHERE l.user = :user
           AND (:cursorId IS NULL OR l.likeId < :cursorId)
       ORDER BY l.likeId DESC
      """)
    List<LikeVinylProjection> findVinylsLikedByUserWithCursor(User user, @Param("cursorId") Long cursorId, Pageable pageable);
    List<Like> findByVinyl(Vinyl vinyl);
    Optional<Like> findByUserAndVinyl(User user, Vinyl vinyl);
}
