package miiiiiin.com.vinyler.application.repository;

import miiiiiin.com.vinyler.application.entity.Follow;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    /** 특정 유저가 팔로우하고 있는 관계 목록 조회 (내가 팔로우한 사람들) */
    List<Follow> findByFollower(User follower);

    /** 특정 유저를 팔로우하고 있는 관계 목록 조회 (나를 팔로우한 사람들) */
    List<Follow> findByFollowing(User following);

    /** 팔로워 → 팔로잉 관계가 존재하는지 조회 (팔로우 여부 확인 및 언팔로우 시 사용) */
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
}
