package miiiiiin.com.vinyler.application.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.request.ReviewRequestDto;
import miiiiiin.com.vinyler.application.dto.response.ReviewResponseDto;
import miiiiiin.com.vinyler.application.entity.Review;
import miiiiiin.com.vinyler.application.service.ReviewService;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
