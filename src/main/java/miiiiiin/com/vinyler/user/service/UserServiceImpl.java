package miiiiiin.com.vinyler.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.VinylDto;
import miiiiiin.com.vinyler.application.entity.Follow;
import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.application.entity.UserVinylStatus;
import miiiiiin.com.vinyler.application.repository.FollowRepository;
import miiiiiin.com.vinyler.application.repository.LikeRepository;
import miiiiiin.com.vinyler.application.repository.UserVinylStatusRepository;
import miiiiiin.com.vinyler.exception.follow.FollowAlreadyExistException;
import miiiiiin.com.vinyler.exception.follow.FollowNotFoundException;
import miiiiiin.com.vinyler.exception.follow.InvalidFollowException;
import miiiiiin.com.vinyler.exception.user.UserAlreadyExistException;
import miiiiiin.com.vinyler.exception.user.UserNotFoundException;
import miiiiiin.com.vinyler.global.Constants;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.dto.UserDto;
import miiiiiin.com.vinyler.user.dto.response.UserResponseDto;
import miiiiiin.com.vinyler.user.entity.User;
import miiiiiin.com.vinyler.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final UserVinylStatusRepository userVinylStatusRepository;
    private final FollowRepository followRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserEntity(username);
        return new UserDetailsImpl(user);
    }

    @Override
    public UserResponseDto registerUser(ServiceRegisterDto dto) {
        // email 중복체크
        userRepository.findByEmail(dto.getEmail()).ifPresent(user -> {
            throw new UserAlreadyExistException(user.getEmail());
        });

        // nickname 중복체크
        userRepository.findByNickname(dto.getNickname()).ifPresent(user -> {
            throw new UserAlreadyExistException(user.getNickname());
        });

        dto.setPasword(passwordEncoder);
        User user = userRepository.save(dto.toEntity());
        return UserResponseDto.from(user);
    }

    @Override
    @Transactional
    public List<VinylDto> getVinylsLikedByUser(Long userId, User currentUser) {
        var userEntity = getUserEntity(userId);

        // LikeRepository 를 통해 유저가 찜한 음반 목록 조회
        List<Like> likedVinyls = likeRepository.findByUser(userEntity);
        return likedVinyls.stream().map(VinylDto::of).toList();
    }

    @Override
    @Transactional
    public List<VinylDto> getVinylsListenedByUser(Long userId, User currentUser) {
        var userEntity = getUserEntity(userId);
        List<UserVinylStatus> listenedVinyls = userVinylStatusRepository.findByUserAndListened(userEntity, true);
        // TODO: FIX (USER ID?)
        return listenedVinyls.stream().map(VinylDto::of).toList();
    }

    @Override
    public UserDto follow(Long userId, User currentUser) {
        var following = getUserEntity(userId);

        if (following.equals(currentUser)) {
            throw new InvalidFollowException(Constants.INVALID_FOLLOW);
        }

        followRepository.findByFollowerAndFollowing(currentUser, following)
                .ifPresent(follow -> {
                    throw new FollowAlreadyExistException();
                });

        followRepository.save(Follow.of(currentUser, following));
        // 팔로우 당하는 사람의 팔로워 숫자 증가
        following.setFollowersCount(following.getFollowersCount() + 1);
        // 팔로우 하는 주체의 팔로우 숫자 증가
        currentUser.setFollowingsCount(following.getFollowingsCount() + 1);

        userRepository.saveAll(List.of(currentUser, following));

        // 팔로잉 당하는 계정 리턴
        return UserDto.from(following, true);
    }

    @Override
    public UserDto unfollow(Long userId, User currentUser) {
        var following = getUserEntity(userId);

        // 스스로를 언팔할 수 없음
        if (following.equals(currentUser)) {
            throw new InvalidFollowException(Constants.INVALID_UNFOLLOW);
        }

        var followEntity = followRepository.findByFollowerAndFollowing(currentUser, following)
                .orElseThrow(() -> new FollowNotFoundException(currentUser, following));

        followRepository.delete(followEntity);

        following.setFollowersCount(Math.max(0, following.getFollowersCount() - 1));
        currentUser.setFollowingsCount(Math.max(0, currentUser.getFollowingsCount() - 1));

        userRepository.saveAll(List.of(currentUser, following));
        return UserDto.from(following, false);
    }

    /**
     * userId의 팔로워 리스트
     */
    @Override
    public List<UserDto> getFollowersByUser(Long userId, User currentUser) {
        // 팔로잉하고 있는 해당 유저 존재하는지 확인
        var following = getUserEntity(userId);
        var followEntities = followRepository.findByFollowing(following);
        return followEntities.stream().map(follow -> getUserWithFollowingStatus(currentUser, follow.getFollower())).toList();
    }

    /**
     * userId가 팔로워 (userId가 팔로잉하고 있는 리스트)
     */
    @Override
    public List<UserDto> getFollowingsByUser(Long userId, User currentUser) {
        var follower = getUserEntity(userId);
        var followEntities = followRepository.findByFollowing(follower);
        return followEntities.stream().map(follow -> getUserWithFollowingStatus(currentUser, follow.getFollowing())).toList();
    }

    /**
     * API를 호출하고 있는 유저가 팔로잉하고 있는지 상태 체크 (팔로워: currentUser, 팔로잉: user)
     */
    private UserDto getUserWithFollowingStatus(User currentUser, User user) {
        boolean isFollowing = followRepository.findByFollowerAndFollowing(currentUser, user).isPresent();
        return UserDto.from(user, isFollowing);
    }

    private User getUserEntity(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
