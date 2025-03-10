package miiiiiin.com.vinyler.application.service;

import miiiiiin.com.vinyler.application.dto.ReviewDto;
import miiiiiin.com.vinyler.application.dto.request.ReviewRequestDto;
import miiiiiin.com.vinyler.application.dto.response.ReviewResponseDto;
import miiiiiin.com.vinyler.user.entity.User;

import java.util.List;

public interface ReviewService {
    ReviewResponseDto createReview(ReviewRequestDto requestDto, User user);
    List<ReviewDto> getReviews(User user);
    ReviewDto getReviewById(Long reviewId, User user);
    ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto requestDto, User user);
}
