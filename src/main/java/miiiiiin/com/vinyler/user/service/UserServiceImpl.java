package miiiiiin.com.vinyler.user.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.VinylDto;
import miiiiiin.com.vinyler.application.dto.projection.LikeVinylProjection;
import miiiiiin.com.vinyler.application.dto.response.SliceResponse;
import miiiiiin.com.vinyler.application.entity.Follow;
import miiiiiin.com.vinyler.application.entity.UserVinylStatus;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.application.repository.FollowRepository;
import miiiiiin.com.vinyler.application.repository.LikeRepository;
import miiiiiin.com.vinyler.application.repository.UserVinylStatusRepository;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import miiiiiin.com.vinyler.exception.follow.FollowAlreadyExistException;
import miiiiiin.com.vinyler.exception.follow.FollowNotFoundException;
import miiiiiin.com.vinyler.exception.follow.InvalidFollowException;
import miiiiiin.com.vinyler.exception.user.UserAlreadyExistException;
import miiiiiin.com.vinyler.exception.user.UserNotAllowedException;
import miiiiiin.com.vinyler.exception.user.UserNotFoundException;
import miiiiiin.com.vinyler.exception.vinyl.VinylNotFoundException;
import miiiiiin.com.vinyler.global.Constants;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.dto.UserDto;
import miiiiiin.com.vinyler.user.dto.request.UpdateUserRequestDto;
import miiiiiin.com.vinyler.user.dto.response.UserResponseDto;
import miiiiiin.com.vinyler.user.entity.User;
import miiiiiin.com.vinyler.user.enums.ProfileVisibility;
import miiiiiin.com.vinyler.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final UserVinylStatusRepository userVinylStatusRepository;
    private final FollowRepository followRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final VinylRepository vinylRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserEntity(username);
        return new UserDetailsImpl(user);
    }

    @Override
    public UserDto getUserInfo(User currentUser) {
        return UserDto.from(currentUser, false);
    }

    @Override
    @Transactional
    public UserDto updateUserInfo(User currentUser, UpdateUserRequestDto dto) {
        if (dto.nickname() != null && !dto.nickname().equals(currentUser.getNickname())) {
            userRepository.findByNickname(dto.nickname()).ifPresent(
                    u -> { throw new UserAlreadyExistException(dto.nickname());});
            currentUser.setNickname(dto.nickname());
        }

        if (dto.profile() != null) currentUser.setProfile(dto.profile());
        if (dto.birthday() != null) currentUser.setBirthday(dto.birthday());
        if (dto.visibility() != null) currentUser.setVisibility(dto.visibility());

        // SecurityContext에서 넘어온 엔티티는 detached 상태일 수 있으므로 명시적으로 저장
        userRepository.save(currentUser);
        return getUserInfo(currentUser);
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
    public SliceResponse<VinylDto> getVinylsLikedByUser(Long userId, User currentUser, Long cursorId, int size) {
        // 커서 페이징 size + 1 (+1로 다음 페이지(hasNext) 존재 여부 판단)
        Pageable pageable = PageRequest.of(0, size + 1);

        var targetUser = getUserEntity(userId);

        // 접근 가능 여부 확인
        validateVisibility(targetUser, currentUser);

        // DTO Projection 활용하여 가져온 사용자 별 찜한 음반 리스트
        List<LikeVinylProjection> results = likeRepository.findVinylsLikedByUserWithCursor(targetUser, cursorId, pageable);

        // 결과가 size보다 많다는 의미
        boolean hasNext = results.size() > size;
        // 마지막 1개를 제외한 size개만 클라이언트에 응답 => subList(0, size)를 이용해 앞쪽 size개만 자름
        List<LikeVinylProjection> contents = hasNext ? results.subList(0, size) : results;

        // 필요한 연관 관계 직접 조회 및 VinylDto로 구성 (유저가 찜한 음반 목록 조회)
        List<VinylDto> vinylDtos = contents.stream()
            .map(projection -> {
                Vinyl vinyl = vinylRepository.findById(projection.getVinylId())
                    .orElseThrow(() -> new VinylNotFoundException(projection.getVinylId()));
                return VinylDto.of(vinyl);
            }).toList();

        // 다음 요청에서 커서로 사용할 ID = 클라이언트에 반환해준 마지막 요소의 ID
        Long nextCursorId = hasNext ? contents.get(contents.size() - 1).getLikeId() : null;

        SliceImpl<VinylDto> sliceContents = new SliceImpl<>(vinylDtos, PageRequest.of(0, size), hasNext);
        return new SliceResponse<>(sliceContents, nextCursorId);
    }

    @Override
    @Transactional
    public SliceResponse<VinylDto> getVinylsListenedByUser(Long userId, User currentUser, Long cursorId, int size) {
        // 커서 페이징 size + 1 (+1로 다음 페이지(hasNext) 존재 여부 판단)
        Pageable pageable = PageRequest.of(0, size + 1);

        var targetUser = getUserEntity(userId);

        // 접근 가능 여부 확인
        validateVisibility(targetUser, currentUser);

        List<UserVinylStatus> results = userVinylStatusRepository.findListenedByUserWithCursor(targetUser, cursorId, pageable);
        // 결과가 size보다 많다는 의미
        boolean hasNext = results.size() > size;
        // 마지막 1개를 제외한 size개만 클라이언트에 응답 => subList(0, size)를 이용해 앞쪽 size개만 자름
        List<UserVinylStatus> contents = hasNext ? results.subList(0, size) : results;

        // 필요한 연관 관계 직접 조회 및 VinylDto로 구성 (유저가 감상한 음반 목록 조회)
        List<VinylDto> vinylDtos = contents.stream()
                .map(VinylDto::of)
                .toList();

        // 다음 요청에서 커서로 사용할 ID = 클라이언트에 반환해준 마지막 요소의 ID (contents.get(last))
        Long nextCursorId = hasNext ? contents.get(contents.size() - 1).getId() : null;

        SliceImpl<VinylDto> sliceContents = new SliceImpl<>(vinylDtos, PageRequest.of(0, size), hasNext);
        return new SliceResponse<>(sliceContents, nextCursorId);
    }



    /**
     * userId의 팔로워 리스트
     * 나를 팔로우한 사람들 (팔로워 목록)
     */
    @Override
    public SliceResponse<UserDto> getFollowersByUser(Long userId, User currentUser, Long cursorId, int size) {
        // 커서 페이징 size + 1 (+1로 다음 페이지(hasNext) 존재 여부 판단)
        Pageable pageable = PageRequest.of(0, size + 1);

        // 팔로잉하고 있는 해당 유저 존재하는지 확인 (팔로워 목록을 조회할 대상 유저)
        var targetUser = getUserEntity(userId);

        // targetUser를 팔로우하고 있는 Follow 행들을 커서 기반으로 조회
        List<Follow> results = followRepository.findFollowersByUserWithCursor(targetUser, cursorId, pageable);

        // 결과가 size보다 많다는 의미
        boolean hasNext = results.size() > size;
        // 마지막 1개를 제외한 size개만 클라이언트에 응답 => subList(0, size)를 이용해 앞쪽 size개만 자름
        List<Follow> contents = hasNext ? results.subList(0, size) : results;

        // follow.getFollower() : 실제 팔로워 유저
        // (currentUser가 각 팔로워를 팔로우하고 있는지 isFollowing 체크 포함)
        List<UserDto> userDtos = contents.stream()
                .map(follow -> getUserWithFollowingStatus(currentUser, follow.getFollower()))
                .toList();

        // 다음 요청에서 커서로 사용할 ID = 클라이언트에 반환해준 마지막 요소의 ID (contents.get(last)) : Follow.id
        Long nextCursorId = hasNext ? contents.get(contents.size() - 1).getId() : null;

        SliceImpl<UserDto> sliceContents = new SliceImpl<>(userDtos, PageRequest.of(0, size), hasNext);
        return new SliceResponse<>(sliceContents, nextCursorId);
    }

    /**
     * userId가 팔로워 (userId가 팔로잉하고 있는 리스트)
     * 내가 팔로우한 사람들 (팔로잉 목록)
     */
    @Override
    public SliceResponse<UserDto> getFollowingsByUser(Long userId, User currentUser, Long cursorId, int size) {
        // 커서 페이징 size + 1 (+1로 다음 페이지(hasNext) 존재 여부 판단)
        Pageable pageable = PageRequest.of(0, size + 1);

        // 팔로잉하고 있는 해당 유저 존재하는지 확인 (팔로워 목록을 조회할 대상 유저)
        var targetUser = getUserEntity(userId);

        // targetUser가 팔로잉하고있는 Follow 행들을 커서 기반으로 조회
        List<Follow> results = followRepository.findFollowingsByUserWithCursor(targetUser, cursorId, pageable);

        // 결과가 size보다 많다는 의미
        boolean hasNext = results.size() > size;
        // 마지막 1개를 제외한 size개만 클라이언트에 응답 => subList(0, size)를 이용해 앞쪽 size개만 자름
        List<Follow> contents = hasNext ? results.subList(0, size) : results;

        // follow.getFollowing : targetUser(나)가 팔로우한 유저
        // (currentUser가 각 팔로워를 팔로우하고 있는지 isFollowing 체크 포함)
        List<UserDto> userDtos = contents.stream()
                .map(follow -> getUserWithFollowingStatus(currentUser, follow.getFollowing()))
                .toList();

        // 다음 요청에서 커서로 사용할 ID = 클라이언트에 반환해준 마지막 요소의 ID (contents.get(last)) : Follow.id
        Long nextCursorId = hasNext ? contents.get(contents.size() - 1).getId() : null;

        SliceImpl<UserDto> sliceContents = new SliceImpl<>(userDtos, PageRequest.of(0, size), hasNext);
        return new SliceResponse<>(sliceContents, nextCursorId);
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
        currentUser.setFollowingsCount(currentUser.getFollowingsCount() + 1);

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

    @Override
    @Transactional
    public void withdrawUser(User currentUser) {
        userRepository.delete(currentUser);
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

    /**
     * 대상 유저의 프로필 공개 범위에 따라 현재 요청자의 접근 가능 여부를 검증
     * PUBLIC이면 통과, FOLLOWERS_ONLY면 팔로우 관계 확인, PRIVATE이면 본인만 허용
     */
    private void validateVisibility(User targetUser, User currentUser) {
        if (targetUser.getVisibility() == ProfileVisibility.PUBLIC) return;

        boolean isSelf = currentUser.getUserId().equals(targetUser.getUserId());
        if (isSelf) return;

        // 프로필 공개 범위 (FOLLOWERS_ONLY)
        if (targetUser.getVisibility() == ProfileVisibility.FOLLOWERS_ONLY) {
            boolean isFollowing = followRepository.findByFollowerAndFollowing(currentUser, targetUser).isPresent();
            if (!isFollowing) throw new UserNotAllowedException();
        } else { // PRIVATE
            throw new UserNotAllowedException();
        }
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
