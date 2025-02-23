package miiiiiin.com.vinyler.application.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.ReviewDto;
import miiiiiin.com.vinyler.application.dto.request.ReviewRequestDto;
import miiiiiin.com.vinyler.application.dto.request.ReviewUpdateRequestDto;
import miiiiiin.com.vinyler.application.dto.response.ReviewResponseDto;
import miiiiiin.com.vinyler.application.entity.Review;
import miiiiiin.com.vinyler.application.service.ReviewService;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "Review 관련 엔드포인트")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/create")
    public ResponseEntity<ReviewResponseDto> createReview(@RequestBody @Valid ReviewRequestDto request,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = reviewService.createReview(request, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getReviews(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = reviewService.getReviews(userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable Long reviewId,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = reviewService.getReviewById(reviewId, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{reviewId}/update")
    public ResponseEntity<ReviewResponseDto> updateReview(@PathVariable Long reviewId, @RequestBody @Valid ReviewRequestDto request,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = reviewService.updateReview(reviewId, request, userDetails.getUser());
        return ResponseEntity.ok(response);
    }
}
