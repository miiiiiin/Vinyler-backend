package miiiiiin.com.vinyler.application.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.ReviewDto;
import miiiiiin.com.vinyler.application.dto.request.ReviewRequestDto;
import miiiiiin.com.vinyler.application.dto.response.ReviewResponseDto;
import miiiiiin.com.vinyler.application.entity.Review;
import miiiiiin.com.vinyler.application.entity.Vinyl;
import miiiiiin.com.vinyler.application.repository.ReviewRepository;
import miiiiiin.com.vinyler.application.repository.UserVinylStatusRepository;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import miiiiiin.com.vinyler.exception.review.ReviewAlreadyExistException;
import miiiiiin.com.vinyler.exception.review.ReviewNotAvailableException;
import miiiiiin.com.vinyler.exception.user.UserAlreadyExistException;
import miiiiiin.com.vinyler.exception.user.UserNotFoundException;
import miiiiiin.com.vinyler.exception.vinyl.VinylNotFoundException;
import miiiiiin.com.vinyler.user.entity.User;
import miiiiiin.com.vinyler.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final VinylRepository vinylRepository;
    private final UserVinylStatusRepository userVinylStatusRepository;

    @Override
    public ReviewResponseDto createReview(ReviewRequestDto request, User currentUser) {
        var vinylEntity = getVinylEntity(request.getDiscogsId());

        // UserVinylStatus 검사 (리뷰 선행 조건 -> 음반 "감상했어요" 상태)
        userVinylStatusRepository.findByUserAndVinyl(currentUser, vinylEntity)
                .orElseThrow(ReviewNotAvailableException::new);

        // 한 유저가 한 음반에 한 개의 리뷰만 남길 수 있도록 검사
        if (reviewRepository.findByUserAndVinyl(currentUser, vinylEntity).isPresent()) {
            throw new ReviewAlreadyExistException();
        }

        var review = reviewRepository.save(Review.of(request.getRating(), request.getContent(), currentUser, vinylEntity));
        return ReviewResponseDto.from(review);
    }

    private Vinyl getVinylEntity(Long discogsId) {
        /**
         * discogs 릴리스 ID 기준 찾기
         */
        return vinylRepository.findByDiscogsId(discogsId)
                .orElseThrow(() -> new VinylNotFoundException(discogsId));
    }
}
