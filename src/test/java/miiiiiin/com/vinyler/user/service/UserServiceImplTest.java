package miiiiiin.com.vinyler.user.service;

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
import miiiiiin.com.vinyler.exception.user.UserNotFoundException;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.dto.UserDto;
import miiiiiin.com.vinyler.user.dto.response.UserResponseDto;
import miiiiiin.com.vinyler.user.dto.request.UpdateUserRequestDto;
import miiiiiin.com.vinyler.user.entity.User;
import miiiiiin.com.vinyler.user.enums.ProfileVisibility;
import miiiiiin.com.vinyler.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserVinylStatusRepository userVinylStatusRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private VinylRepository vinylRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User anotherUser;
    private ServiceRegisterDto registerDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("testuser")
                .password("encodedPassword")
                .followersCount(0L)
                .followingsCount(0L)
                .build();

        anotherUser = User.builder()
                .userId(2L)
                .email("another@example.com")
                .nickname("anotheruser")
                .password("encodedPassword")
                .followersCount(0L)
                .followingsCount(0L)
                .build();

        registerDto = ServiceRegisterDto.builder()
                .email("newuser@example.com")
                .password("password123")
                .nickname("newuser")
                .profile("profile.jpg")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    @DisplayName("사용자 등록 - 성공")
    void registerUser_Success() {
        // Given
        when(userRepository.findByEmail(registerDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByNickname(registerDto.getNickname())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponseDto result = userService.registerUser(registerDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getNickname()).isEqualTo(testUser.getNickname());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 등록 - 이메일 중복 시 예외 발생")
    void registerUser_EmailExists_ThrowsException() {
        // Given
        when(userRepository.findByEmail(registerDto.getEmail())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(registerDto))
                .isInstanceOf(UserAlreadyExistException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 등록 - 닉네임 중복 시 예외 발생")
    void registerUser_NicknameExists_ThrowsException() {
        // Given
        when(userRepository.findByEmail(registerDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByNickname(registerDto.getNickname())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(registerDto))
                .isInstanceOf(UserAlreadyExistException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자명으로 사용자 로드 - 성공")
    void loadUserByUsername_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = userService.loadUserByUsername("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test@example.com");
        assertThat(result).isInstanceOf(UserDetailsImpl.class);
    }

    @Test
    @DisplayName("사용자명으로 사용자 로드 - 사용자 없음 시 예외 발생")
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 정보 조회 - 성공")
    void getUserInfo_Success() {
        // When
        UserDto result = userService.getUserInfo(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getNickname()).isEqualTo(testUser.getNickname());
        assertThat(result.getVisibility()).isEqualTo(ProfileVisibility.PUBLIC);
        assertThat(result.isFollowing()).isFalse();
    }

    @Test
    @DisplayName("유저 정보 수정 - 전체 필드 성공")
    void updateUserInfo_AllFields_Success() {
        // Given
        UpdateUserRequestDto dto = new UpdateUserRequestDto(
                "newnickname", "newprofile.jpg", LocalDate.of(1995, 5, 15), ProfileVisibility.PRIVATE
        );
        when(userRepository.findByNickname("newnickname")).thenReturn(Optional.empty());
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        UserDto result = userService.updateUserInfo(testUser, dto);

        // Then
        assertThat(result.getNickname()).isEqualTo("newnickname");
        assertThat(result.getProfile()).isEqualTo("newprofile.jpg");
        assertThat(result.getBirthday()).isEqualTo(LocalDate.of(1995, 5, 15));
        assertThat(result.getVisibility()).isEqualTo(ProfileVisibility.PRIVATE);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("유저 정보 수정 - 현재 닉네임과 동일하면 중복 체크 없음")
    void updateUserInfo_SameNickname_NoCheck() {
        // Given
        UpdateUserRequestDto dto = new UpdateUserRequestDto("testuser", null, null, null);
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        userService.updateUserInfo(testUser, dto);

        // Then
        verify(userRepository, never()).findByNickname(any());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("유저 정보 수정 - 닉네임 중복 시 예외 발생")
    void updateUserInfo_NicknameDuplicate_ThrowsException() {
        // Given
        UpdateUserRequestDto dto = new UpdateUserRequestDto("duplicatenick", null, null, null);
        when(userRepository.findByNickname("duplicatenick")).thenReturn(Optional.of(anotherUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateUserInfo(testUser, dto))
                .isInstanceOf(UserAlreadyExistException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("유저 정보 수정 - null 필드는 변경 없음")
    void updateUserInfo_NullFields_NoChange() {
        // Given
        UpdateUserRequestDto dto = new UpdateUserRequestDto(null, null, null, null);
        String originalNickname = testUser.getNickname();
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        UserDto result = userService.updateUserInfo(testUser, dto);

        // Then
        assertThat(result.getNickname()).isEqualTo(originalNickname);
        verify(userRepository, never()).findByNickname(any());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("팔로우 - 성공")
    void follow_Success() {
        // Given
        when(userRepository.findById(2L)).thenReturn(Optional.of(anotherUser));
        when(followRepository.findByFollowerAndFollowing(testUser, anotherUser)).thenReturn(Optional.empty());
        when(followRepository.save(any(Follow.class))).thenReturn(new Follow());
        when(userRepository.saveAll(anyList())).thenReturn(Arrays.asList(testUser, anotherUser));

        // When
        UserDto result = userService.follow(2L, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFollowing()).isTrue();
        assertThat(anotherUser.getFollowersCount()).isEqualTo(1L);
        verify(followRepository, times(1)).save(any(Follow.class));
    }

    @Test
    @DisplayName("팔로우 - 자기 자신을 팔로우할 때 예외 발생")
    void follow_SelfFollow_ThrowsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.follow(1L, testUser))
                .isInstanceOf(InvalidFollowException.class);
        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    @DisplayName("팔로우 - 이미 팔로우 중일 때 예외 발생")
    void follow_AlreadyFollowing_ThrowsException() {
        // Given
        Follow existingFollow = Follow.of(testUser, anotherUser);
        when(userRepository.findById(2L)).thenReturn(Optional.of(anotherUser));
        when(followRepository.findByFollowerAndFollowing(testUser, anotherUser))
                .thenReturn(Optional.of(existingFollow));

        // When & Then
        assertThatThrownBy(() -> userService.follow(2L, testUser))
                .isInstanceOf(FollowAlreadyExistException.class);
        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    @DisplayName("언팔로우 - 성공")
    void unfollow_Success() {
        // Given
        Follow existingFollow = Follow.of(testUser, anotherUser);
        testUser.setFollowingsCount(1L);
        anotherUser.setFollowersCount(1L);
        
        when(userRepository.findById(2L)).thenReturn(Optional.of(anotherUser));
        when(followRepository.findByFollowerAndFollowing(testUser, anotherUser))
                .thenReturn(Optional.of(existingFollow));
        when(userRepository.saveAll(anyList())).thenReturn(Arrays.asList(testUser, anotherUser));

        // When
        UserDto result = userService.unfollow(2L, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isFollowing()).isFalse();
        assertThat(anotherUser.getFollowersCount()).isEqualTo(0L);
        assertThat(testUser.getFollowingsCount()).isEqualTo(0L);
        verify(followRepository, times(1)).delete(existingFollow);
    }

    @Test
    @DisplayName("언팔로우 - 자기 자신을 언팔로우할 때 예외 발생")
    void unfollow_SelfUnfollow_ThrowsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.unfollow(1L, testUser))
                .isInstanceOf(InvalidFollowException.class);
        verify(followRepository, never()).delete(any(Follow.class));
    }

    @Test
    @DisplayName("언팔로우 - 팔로우하지 않은 사용자를 언팔로우할 때 예외 발생")
    void unfollow_NotFollowing_ThrowsException() {
        // Given
        when(userRepository.findById(2L)).thenReturn(Optional.of(anotherUser));
        when(followRepository.findByFollowerAndFollowing(testUser, anotherUser))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.unfollow(2L, testUser))
                .isInstanceOf(FollowNotFoundException.class);
        verify(followRepository, never()).delete(any(Follow.class));
    }

    @Test
    @DisplayName("팔로워 목록 조회 - 성공")
    void getFollowersByUser_Success() {
        // Given
        Follow follow1 = Follow.of(anotherUser, testUser);
        User thirdUser = User.builder()
                .userId(3L)
                .email("third@example.com")
                .nickname("thirduser")
                .build();
        Follow follow2 = Follow.of(thirdUser, testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(followRepository.findByFollowing(testUser)).thenReturn(Arrays.asList(follow1, follow2));
        when(followRepository.findByFollowerAndFollowing(eq(testUser), any())).thenReturn(Optional.empty());

        // When
        List<UserDto> result = userService.getFollowersByUser(1L, testUser);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("email").contains("another@example.com", "third@example.com");
    }

    @Test
    @DisplayName("팔로잉 목록 조회 - 성공")
    void getFollowingsByUser_Success() {
        // Given
        Follow follow1 = Follow.of(testUser, anotherUser);
        User thirdUser = User.builder()
                .userId(3L)
                .email("third@example.com")
                .nickname("thirduser")
                .build();
        Follow follow2 = Follow.of(testUser, thirdUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(followRepository.findByFollower(testUser)).thenReturn(Arrays.asList(follow1, follow2));
        when(followRepository.findByFollowerAndFollowing(eq(testUser), any())).thenReturn(Optional.empty());

        // When
        List<UserDto> result = userService.getFollowingsByUser(1L, testUser);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("사용자가 좋아요한 Vinyl 목록 조회 - 커서 페이징")
    void getVinylsLikedByUser_WithCursorPaging() {
        // Given
        Long userId = 1L;
        Long cursorId = null;
        int size = 2;

        Vinyl vinyl1 = Vinyl.builder().vinylId(1L).discogsId(100L).title("Vinyl 1")
                .images(new ArrayList<>()).tracklist(new ArrayList<>()).formats(new ArrayList<>())
                .videos(new ArrayList<>()).artists(new ArrayList<>()).build();
        
        LikeVinylProjection projection1 = mock(LikeVinylProjection.class);
        when(projection1.getLikeId()).thenReturn(1L);
        when(projection1.getVinylId()).thenReturn(1L);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(likeRepository.findVinylsLikedByUserWithCursor(eq(testUser), eq(cursorId), any()))
                .thenReturn(List.of(projection1));
        when(vinylRepository.findById(1L)).thenReturn(Optional.of(vinyl1));

        // When
        SliceResponse result = userService.getVinylsLikedByUser(userId, testUser, cursorId, size);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("사용자가 감상한 Vinyl 목록 조회 - 성공")
    void getVinylsListenedByUser_Success() {
        // Given
        Long cursorId = null;
        int size = 2;
        Vinyl vinyl1 = Vinyl.builder().vinylId(1L).discogsId(100L).title("Vinyl 1")
                .images(new ArrayList<>()).tracklist(new ArrayList<>()).formats(new ArrayList<>())
                .videos(new ArrayList<>()).artists(new ArrayList<>()).build();
        
        UserVinylStatus status1 = UserVinylStatus.of(testUser, vinyl1, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userVinylStatusRepository.findListenedByUserWithCursor(eq(testUser), isNull(), any()))
                .thenReturn(List.of(status1));

        // When
        SliceResponse<VinylDto> result = userService.getVinylsListenedByUser(1L, testUser, cursorId, size);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
    void getUserEntity_NotFound_ThrowsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.follow(999L, testUser))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("회원 탈퇴 - soft delete 호출 성공")
    void withdrawUser_Success() {
        // When
        userService.withdrawUser(testUser);

        // Then
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("회원 탈퇴 - delete 호출 후 추가 save 없음")
    void withdrawUser_NoExtraSave() {
        // When
        userService.withdrawUser(testUser);

        // Then
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).saveAll(anyList());
    }
}