package miiiiiin.com.vinyler.application.repository;

import miiiiiin.com.vinyler.application.entity.Follow;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findByFollower(User follower);
    List<Follow> findByFollowing(User following);
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
}
