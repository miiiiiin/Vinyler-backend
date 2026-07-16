package miiiiiin.com.vinyler.application.repository;

import miiiiiin.com.vinyler.application.entity.UserVinylStatus;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserVinylStatusRepository extends JpaRepository<UserVinylStatus, Long> {
    // 특정 사용자가 특정 LP의 상태를 조회
//    Optional<UserVinylStatus> findByUserIdAndVinylId(Long userId, Long vinylId);
    Optional<UserVinylStatus> findByUserAndVinyl(User user, Vinyl vinyl);
    // 특정 사용자가 감상한 LP들을 조회
    List<UserVinylStatus> findByUserAndListened(User user, boolean listened);


    @Query("""
        SELECT uvs 
        FROM UserVinylStatus uvs      
        WHERE uvs.user = :user
        AND uvs.listened = true
        AND (:cursorId IS NULL OR uvs.id < :cursorId)
        ORDER BY uvs.id DESC
      """
    )
    List<UserVinylStatus> findListenedByUserWithCursor(@Param("user") User user, @Param("cursorId") Long cursorId, Pageable pageable);



}
