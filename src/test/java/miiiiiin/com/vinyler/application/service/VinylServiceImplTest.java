package miiiiiin.com.vinyler.application.service;

import miiiiiin.com.vinyler.application.dto.VinylDetailDto;
import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.application.entity.UserVinylStatus;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.application.repository.LikeRepository;
import miiiiiin.com.vinyler.application.repository.UserVinylStatusRepository;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import miiiiiin.com.vinyler.global.Constants;
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
@DisplayName("VinylService 단위 테스트")
class VinylServiceImplTest {

    @Mock
    private VinylRepository vinylRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserVinylStatusRepository userVinylStatusRepository;

    @InjectMocks
    private VinylServiceImpl vinylService;

    private User testUser;
    private Vinyl testVinyl;
    private LikeRequestDto likeRequestDto;
    private Like testLike;

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
                .likesCount(5L)
                .reviewsCount(3L)
                .build();

        likeRequestDto = new LikeRequestDto(
                12345L,
                "Test Album",
                "Test Artist",
                "2023"
        );

        testLike = Like.of(testUser, testVinyl);
    }

    @Test
    @DisplayName("좋아요 토글 - 좋아요 추가 (Vinyl이 이미 존재)")
    void toggleLike_AddLike_VinylExists() {
        // Given
        when(vinylRepository.findByDiscogsId(12345L)).thenReturn(Optional.of(testVinyl));
        when(likeRepository.findByUserAndVinyl(testUser, testVinyl)).thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class))).thenReturn(testLike);
        when(vinylRepository.save(any(Vinyl.class))).thenReturn(testVinyl);

        // When
        VinylLikeDto result = vinylService.toggleLike(likeRequestDto, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isLiking()).isTrue();
        assertThat(result.getVinylId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(testVinyl.getLikesCount()).isEqualTo(6L);
        verify(likeRepository, times(1)).save(any(Like.class));
        verify(vinylRepository, times(1)).save(testVinyl);
    }

    @Test
    @DisplayName("좋아요 토글 - 좋아요 추가 (Vinyl이 존재하지 않음)")
    void toggleLike_AddLike_VinylNotExists() {
        // Given
        when(vinylRepository.findByDiscogsId(12345L)).thenReturn(Optional.empty());
        when(vinylRepository.save(any(Vinyl.class))).thenReturn(testVinyl);
        when(likeRepository.findByUserAndVinyl(testUser, testVinyl)).thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class))).thenReturn(testLike);

        // When
        VinylLikeDto result = vinylService.toggleLike(likeRequestDto, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isLiking()).isTrue();
        verify(vinylRepository, times(2)).save(any(Vinyl.class)); // 한 번은 생성, 한 번은 likesCount 업데이트
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    @DisplayName("좋아요 토글 - 좋아요 취소")
    void toggleLike_RemoveLike() {
        // Given
        when(vinylRepository.findByDiscogsId(12345L)).thenReturn(Optional.of(testVinyl));
        when(likeRepository.findByUserAndVinyl(testUser, testVinyl)).thenReturn(Optional.of(testLike));
        when(vinylRepository.save(any(Vinyl.class))).thenReturn(testVinyl);

        // When
        VinylLikeDto result = vinylService.toggleLike(likeRequestDto, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isLiking()).isFalse();
        assertThat(testVinyl.getLikesCount()).isEqualTo(4L);
        verify(likeRepository, times(1)).delete(testLike);
        verify(vinylRepository, times(1)).save(testVinyl);
    }

    @Test
    @DisplayName("좋아요 토글 - likesCount가 0보다 작아지지 않음")
    void toggleLike_RemoveLike_LikesCountNotNegative() {
        // Given
        testVinyl.setLikesCount(0L);
        when(vinylRepository.findByDiscogsId(12345L)).thenReturn(Optional.of(testVinyl));
        when(likeRepository.findByUserAndVinyl(testUser, testVinyl)).thenReturn(Optional.of(testLike));
        when(vinylRepository.save(any(Vinyl.class))).thenReturn(testVinyl);

        // When
        VinylLikeDto result = vinylService.toggleLike(likeRequestDto, testUser);

        // Then
        assertThat(testVinyl.getLikesCount()).isEqualTo(0L);
        assertThat(result.isLiking()).isFalse();
    }

    @Test
    @DisplayName("Vinyl 상세 조회 - 찜/감상 모두 한 경우")
    void getVinylDetail_LikingAndListened() {
        // Given
        UserVinylStatus listenedStatus = UserVinylStatus.of(testUser, testVinyl, true);
        when(vinylRepository.findByDiscogsId(12345L)).thenReturn(Optional.of(testVinyl));
        when(likeRepository.findByUserAndVinyl(testUser, testVinyl)).thenReturn(Optional.of(testLike));
        when(userVinylStatusRepository.findByUserAndVinyl(testUser, testVinyl)).thenReturn(Optional.of(listenedStatus));

        // When
        VinylDetailDto result = vinylService.getVinylDetail(12345L, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isLiking()).isTrue();
        assertThat(result.isListened()).isTrue();
        assertThat(result.getVinylId()).isEqualTo(1L);
        assertThat(result.getDiscogsId()).isEqualTo(12345L);
        assertThat(result.getLikesCount()).isEqualTo(5L);
        assertThat(result.getReviewsCount()).isEqualTo(3L);
        verify(vinylRepository, times(1)).findByDiscogsId(12345L);
        verify(likeRepository, times(1)).findByUserAndVinyl(testUser, testVinyl);
    }

    @Test
    @DisplayName("Vinyl 상세 조회 - 찜/감상 모두 하지 않은 경우")
    void getVinylDetail_NotLikingNotListened() {
        // Given
        when(vinylRepository.findByDiscogsId(12345L)).thenReturn(Optional.of(testVinyl));
        when(likeRepository.findByUserAndVinyl(testUser, testVinyl)).thenReturn(Optional.empty());
        when(userVinylStatusRepository.findByUserAndVinyl(testUser, testVinyl)).thenReturn(Optional.empty());

        // When
        VinylDetailDto result = vinylService.getVinylDetail(12345L, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isLiking()).isFalse();
        assertThat(result.isListened()).isFalse();
        verify(vinylRepository, times(1)).findByDiscogsId(12345L);
        verify(likeRepository, times(1)).findByUserAndVinyl(testUser, testVinyl);
    }

    @Test
    @DisplayName("Vinyl 상세 조회 - Vinyl이 존재하지 않을 때 예외 발생")
    void getVinylDetail_VinylNotFound_ThrowsException() {
        // Given
        when(vinylRepository.findByDiscogsId(99999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> vinylService.getVinylDetail(99999L, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(Constants.ALBUM_NOT_FOUND);
        verify(likeRepository, never()).findByUserAndVinyl(any(), any());
    }
}