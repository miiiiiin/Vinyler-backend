package miiiiiin.com.vinyler.application.service;

import miiiiiin.com.vinyler.application.dto.UserVinylStatusDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.entity.UserVinylStatus;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.application.repository.UserVinylStatusRepository;
import miiiiiin.com.vinyler.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserVinylStatusService 단위 테스트")
class UserVinylServiceStatusImplTest {

    @Mock
    private VinylService vinylService;

    @Mock
    private UserVinylStatusRepository userVinylStatusRepository;

    @InjectMocks
    private UserVinylServiceStatusImpl userVinylStatusService;

    private User testUser;
    private Vinyl testVinyl;
    private LikeRequestDto likeRequestDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("testuser")
                .build();

        testVinyl = Vinyl.builder()
                .vinylId(1L)
                .discogsId(12345L)
                .title("Test Album")
                .artistsSort("Test Artist")
                .build();

        likeRequestDto = new LikeRequestDto(
                12345L,
                "Test Album",
                "Test Artist",
                "2023"
        );
    }

    @Test
    @DisplayName("감상 상태 토글 - 감상 표시 추가 (Vinyl 존재)")
    void toggleListenStatus_AddListen_VinylExists() {
        // Given
        when(vinylService.findOrCreateVinyl(likeRequestDto, testUser)).thenReturn(testVinyl);
        when(userVinylStatusRepository.findByUserAndVinyl(testUser, testVinyl)).thenReturn(Optional.empty());
        when(userVinylStatusRepository.save(any(UserVinylStatus.class))).thenReturn(new UserVinylStatus());

        // When
        UserVinylStatusDto result = userVinylStatusService.toggleListenStatus(likeRequestDto, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isListened()).isTrue();
        assertThat(result.getVinylId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
        verify(userVinylStatusRepository, times(1)).save(any(UserVinylStatus.class));
    }

    @Test
    @DisplayName("감상 상태 토글 - 감상 표시 추가 (Vinyl 존재하지 않음)")
    void toggleListenStatus_AddListen_VinylNotExists() {
        // Given
        when(vinylService.findOrCreateVinyl(likeRequestDto, testUser)).thenReturn(testVinyl);
        when(userVinylStatusRepository.findByUserAndVinyl(testUser, testVinyl)).thenReturn(Optional.empty());
        when(userVinylStatusRepository.save(any(UserVinylStatus.class))).thenReturn(new UserVinylStatus());

        // When
        UserVinylStatusDto result = userVinylStatusService.toggleListenStatus(likeRequestDto, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isListened()).isTrue();
        verify(userVinylStatusRepository, times(1)).save(any(UserVinylStatus.class));
    }

    @Test
    @DisplayName("감상 상태 토글 - 감상 표시 제거")
    void toggleListenStatus_RemoveListen() {
        // Given
        UserVinylStatus existingStatus = UserVinylStatus.of(testUser, testVinyl, true);

        when(vinylService.findOrCreateVinyl(likeRequestDto, testUser)).thenReturn(testVinyl);
        when(userVinylStatusRepository.findByUserAndVinyl(testUser, testVinyl))
                .thenReturn(Optional.of(existingStatus));

        // When
        UserVinylStatusDto result = userVinylStatusService.toggleListenStatus(likeRequestDto, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isListened()).isFalse();
        verify(userVinylStatusRepository, times(1)).delete(existingStatus);
    }

    @Test
    @DisplayName("감상 상태 토글 - 감상하지 않은 상태에서 토글")
    void toggleListenStatus_FromNotListenedToListened() {
        // Given
        UserVinylStatus existingStatus = UserVinylStatus.of(testUser, testVinyl, false);

        when(vinylService.findOrCreateVinyl(likeRequestDto, testUser)).thenReturn(testVinyl);
        when(userVinylStatusRepository.findByUserAndVinyl(testUser, testVinyl))
                .thenReturn(Optional.of(existingStatus));
        when(userVinylStatusRepository.save(any(UserVinylStatus.class))).thenReturn(existingStatus);

        // When
        UserVinylStatusDto result = userVinylStatusService.toggleListenStatus(likeRequestDto, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isListened()).isTrue();
        verify(userVinylStatusRepository, times(1)).save(any(UserVinylStatus.class));
    }

    @Test
    @DisplayName("감상 상태 토글 - 여러 번 토글 테스트")
    void toggleListenStatus_MultipleToggles() {
        // Given - 첫 번째 토글 (추가)
        when(vinylService.findOrCreateVinyl(likeRequestDto, testUser)).thenReturn(testVinyl);
        when(userVinylStatusRepository.findByUserAndVinyl(testUser, testVinyl)).thenReturn(Optional.empty());
        when(userVinylStatusRepository.save(any(UserVinylStatus.class))).thenReturn(new UserVinylStatus());

        // When - 첫 번째 토글
        UserVinylStatusDto result1 = userVinylStatusService.toggleListenStatus(likeRequestDto, testUser);

        // Then - 첫 번째 토글 검증
        assertThat(result1.isListened()).isTrue();

        // Given - 두 번째 토글 (제거)
        UserVinylStatus existingStatus = UserVinylStatus.of(testUser, testVinyl, true);
        when(userVinylStatusRepository.findByUserAndVinyl(testUser, testVinyl))
                .thenReturn(Optional.of(existingStatus));

        // When - 두 번째 토글
        UserVinylStatusDto result2 = userVinylStatusService.toggleListenStatus(likeRequestDto, testUser);

        // Then - 두 번째 토글 검증
        assertThat(result2.isListened()).isFalse();
    }
}
