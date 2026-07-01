package miiiiiin.com.vinyler.application.service;

import miiiiiin.com.vinyler.application.dto.ReviewDto;
import miiiiiin.com.vinyler.application.dto.request.ReviewRequestDto;
import miiiiiin.com.vinyler.application.dto.response.ReviewResponseDto;
import miiiiiin.com.vinyler.application.dto.response.SliceResponse;
import miiiiiin.com.vinyler.application.entity.Review;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.application.repository.ReviewRepository;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import miiiiiin.com.vinyler.exception.review.ReviewAlreadyExistException;
import miiiiiin.com.vinyler.exception.review.ReviewNotFoundException;
import miiiiiin.com.vinyler.exception.user.UserNotAllowedException;
import miiiiiin.com.vinyler.exception.vinyl.VinylNotFoundException;
import miiiiiin.com.vinyler.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 단위 테스트")
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private VinylRepository vinylRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User testUser;
    private Vinyl testVinyl;
    private Review testReview;
    private ReviewRequestDto reviewRequestDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("testuser")
                .password("password123")
                .build();

        testVinyl = Vinyl.builder()
                .vinylId(1L)
                .discogsId(12345L)
                .title("Test Album")
                .artistsSort("Test Artist")
                .reviewsCount(0L)
                .build();

        testReview = Review.builder()
                .id(1L)
                .rating(5)
                .content("Great album!")
                .user(testUser)
                .vinyl(testVinyl)
                .build();

        reviewRequestDto = new ReviewRequestDto();
        reviewRequestDto.setDiscogsId(12345L);
        reviewRequestDto.setRating(5);
        reviewRequestDto.setContent("Great album!");
    }

    @Test
    @DisplayName("리뷰 생성 - 성공")
    void createReview_Success() {
        // Given
        when(vinylRepository.findByDiscogsId(12345L)).thenReturn(Optional.of(testVinyl));
        when(reviewRepository.findByUserAndVinyl(testUser, testVinyl)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(vinylRepository.save(any(Vinyl.class))).thenReturn(testVinyl);

        // When
        ReviewResponseDto result = reviewService.createReview(reviewRequestDto, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReviewId()).isEqualTo(1L);
        assertThat(result.getDiscogsId()).isEqualTo(12345L);
        assertThat(testVinyl.getReviewsCount()).isEqualTo(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(vinylRepository, times(1)).save(testVinyl);
    }

    @Test
    @DisplayName("리뷰 생성 - Vinyl이 존재하지 않을 때 예외 발생")
    void createReview_VinylNotFound_ThrowsException() {
        // Given
        when(vinylRepository.findByDiscogsId(12345L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(reviewRequestDto, testUser))
                .isInstanceOf(VinylNotFoundException.class);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 생성 - 이미 리뷰가 존재할 때 예외 발생")
    void createReview_ReviewAlreadyExists_ThrowsException() {
        // Given
        when(vinylRepository.findByDiscogsId(12345L)).thenReturn(Optional.of(testVinyl));
        when(reviewRepository.findByUserAndVinyl(testUser, testVinyl)).thenReturn(Optional.of(testReview));

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(reviewRequestDto, testUser))
                .isInstanceOf(ReviewAlreadyExistException.class);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("사용자의 모든 리뷰 조회 - 성공")
    void getReviews_Success() {
        // Given
        Review review2 = Review.builder()
                .id(2L)
                .rating(4)
                .content("Good album")
                .user(testUser)
                .vinyl(testVinyl)
                .build();
        
        when(reviewRepository.findByUser(testUser)).thenReturn(Arrays.asList(testReview, review2));

        // When
        List<ReviewDto> result = reviewService.getReviews(testUser);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        verify(reviewRepository, times(1)).findByUser(testUser);
    }

    @Test
    @DisplayName("사용자의 리뷰 조회 - 리뷰가 없는 경우")
    void getReviews_EmptyList() {
        // Given
        when(reviewRepository.findByUser(testUser)).thenReturn(List.of());

        // When
        List<ReviewDto> result = reviewService.getReviews(testUser);

        // Then
        assertThat(result).isEmpty();
        verify(reviewRepository, times(1)).findByUser(testUser);
    }

    @Test
    @DisplayName("리뷰 ID로 조회 - 성공")
    void getReviewById_Success() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When
        ReviewDto result = reviewService.getReviewById(1L, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getContent()).isEqualTo("Great album!");
        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("리뷰 ID로 조회 - 리뷰가 존재하지 않을 때 예외 발생")
    void getReviewById_NotFound_ThrowsException() {
        // Given
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.getReviewById(999L, testUser))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining("999");
        verify(reviewRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("리뷰 수정 - 성공")
    void updateReview_Success() {
        // Given
        ReviewRequestDto updateRequest = new ReviewRequestDto();
        updateRequest.setDiscogsId(12345L);
        updateRequest.setRating(4);
        updateRequest.setContent("Updated review content");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When
        ReviewResponseDto result = reviewService.updateReview(1L, updateRequest, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReviewId()).isEqualTo(1L);
        assertThat(testReview.getRating()).isEqualTo(4);
        assertThat(testReview.getContent()).isEqualTo("Updated review content");
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 수정 - 리뷰가 존재하지 않을 때 예외 발생")
    void updateReview_NotFound_ThrowsException() {
        // Given
        ReviewRequestDto updateRequest = new ReviewRequestDto();
        updateRequest.setDiscogsId(12345L);
        updateRequest.setRating(4);
        updateRequest.setContent("Updated review");

        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.updateReview(999L, updateRequest, testUser))
                .isInstanceOf(ReviewNotFoundException.class);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 수정 - 다른 사용자가 수정 시도할 때 예외 발생")
    void updateReview_NotAuthor_ThrowsException() {
        // Given
        User anotherUser = User.builder()
                .userId(2L)
                .email("another@example.com")
                .nickname("anotheruser")
                .build();

        ReviewRequestDto updateRequest = new ReviewRequestDto();
        updateRequest.setDiscogsId(12345L);
        updateRequest.setRating(4);
        updateRequest.setContent("Updated review");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When & Then
        assertThatThrownBy(() -> reviewService.updateReview(1L, updateRequest, anotherUser))
                .isInstanceOf(UserNotAllowedException.class);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Discogs ID로 리뷰 목록 조회 - 커서 페이징 성공")
    void getReviewsByDiscogsId_WithCursor_Success() {
        // Given
        Long discogsId = 12345L;
        Long cursorId = null;
        int size = 10;

        Review review2 = Review.builder()
                .id(2L)
                .rating(4)
                .content("Another review")
                .user(testUser)
                .vinyl(testVinyl)
                .build();

        when(vinylRepository.findByDiscogsId(discogsId)).thenReturn(Optional.of(testVinyl));
        when(reviewRepository.findByVinylWithCursor(eq(testVinyl), eq(cursorId), any()))
                .thenReturn(Arrays.asList(testReview, review2));

        // When
        SliceResponse result = reviewService.getReviewsByDiscogsId(discogsId, cursorId, size);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.isHasNext()).isFalse();
        verify(vinylRepository, times(1)).findByDiscogsId(discogsId);
    }

    @Test
    @DisplayName("Discogs ID로 리뷰 조회 - Vinyl이 존재하지 않을 때 예외 발생")
    void getReviewsByDiscogsId_VinylNotFound_ThrowsException() {
        // Given
        Long discogsId = 99999L;
        when(vinylRepository.findByDiscogsId(discogsId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.getReviewsByDiscogsId(discogsId, null, 10))
                .isInstanceOf(VinylNotFoundException.class);
    }

    @Test
    @DisplayName("Discogs ID로 리뷰 조회 - hasNext가 true인 경우")
    void getReviewsByDiscogsId_HasNextIsTrue() {
        // Given
        Long discogsId = 12345L;
        int size = 2;
        
        // size + 1개의 데이터 반환 (hasNext 확인용)
        Review review2 = Review.builder().id(2L).rating(4).content("Review 2").user(testUser).vinyl(testVinyl).build();
        Review review3 = Review.builder().id(3L).rating(3).content("Review 3").user(testUser).vinyl(testVinyl).build();

        when(vinylRepository.findByDiscogsId(discogsId)).thenReturn(Optional.of(testVinyl));
        when(reviewRepository.findByVinylWithCursor(eq(testVinyl), isNull(), any()))
                .thenReturn(Arrays.asList(testReview, review2, review3));

        // When
        SliceResponse result = reviewService.getReviewsByDiscogsId(discogsId, null, size);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursorId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("리뷰 ID로 리뷰 삭제 - 본인 리뷰를 삭제하는 정상 케이스")
    void deleteReview_success() {
        // Given (testUser가 작성한 리뷰 반환)
        long reviewId = 1L;
        testVinyl.setReviewsCount(1L);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
        when(vinylRepository.save(any(Vinyl.class))).thenReturn(testVinyl);

        // when
        reviewService.deleteReview(reviewId, testUser);

        // then
        assertThat(testVinyl.getReviewsCount()).isEqualTo(0L);
        verify(reviewRepository, times(1)).delete(testReview);
        verify(vinylRepository, times(1)).save(testVinyl);
    }
    @Test
    @DisplayName("리뷰 ID로 리뷰 삭제 - 존재하지 않는 reviewId로 삭제 시도")
    void deleteReview_ReviewNotFound_ThrowsException() {
        // Given (존재하지 않는 리뷰 반환)
        long reviewId = 999L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> reviewService.deleteReview(reviewId, testUser))
                .isInstanceOf(ReviewNotFoundException.class);

        // then
        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 ID로 리뷰 삭제 -  anotherUser가 작성한 리뷰를 testUser가 삭제 시도")
    void deleteReview_NotOwner_ThrowsException() {
        // Given
        User anotherUser = User.builder()
                .userId(2L)
                .email("another@example.com")
                .nickname("anotheruser")
                .build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview)); // testUser가 작성한 리뷰


        // when
        assertThatThrownBy(() -> reviewService.deleteReview(1L, anotherUser))
                .isInstanceOf(UserNotAllowedException.class);

        // then
        verify(reviewRepository, never()).delete(any(Review.class));
    }
}