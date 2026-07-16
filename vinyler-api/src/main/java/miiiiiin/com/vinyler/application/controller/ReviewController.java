package miiiiiin.com.vinyler.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.ReviewDto;
import miiiiiin.com.vinyler.application.dto.request.ReviewRequestDto;
import miiiiiin.com.vinyler.application.dto.response.ReviewResponseDto;
import miiiiiin.com.vinyler.application.dto.response.SliceResponse;
import miiiiiin.com.vinyler.application.service.ReviewService;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 관련 엔드포인트")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/create")
    @Operation(summary = "리뷰 작성", description = "Vinyl에 대한 리뷰를 작성합니다. 한 유저당 한 Vinyl에 하나의 리뷰만 작성 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 작성 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "409", description = "이미 작성한 리뷰가 존재함")
    })
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestBody @Valid ReviewRequestDto request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = reviewService.createReview(request, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "내 리뷰 목록 조회", description = "현재 로그인한 사용자가 작성한 모든 리뷰를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<List<ReviewDto>> getReviews(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = reviewService.getReviews(userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "리뷰 단건 조회", description = "리뷰 ID로 특정 리뷰를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    public ResponseEntity<ReviewDto> getReviewById(
            @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = reviewService.getReviewById(reviewId, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{reviewId}/update")
    @Operation(summary = "리뷰 수정", description = "작성한 리뷰의 평점과 내용을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    public ResponseEntity<ReviewResponseDto> updateReview(
            @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
            @RequestBody @Valid ReviewRequestDto request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = reviewService.updateReview(reviewId, request, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/discogs/{discogsId}")
    @Operation(summary = "Vinyl 리뷰 목록 조회", description = "특정 Vinyl(Discogs ID 기준)의 리뷰를 커서 기반 페이징으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "Vinyl을 찾을 수 없음")
    })
    public ResponseEntity<SliceResponse<ReviewDto>> getReviewsByDiscogsId(
            @Parameter(description = "Discogs Release ID", example = "1234567") @PathVariable Long discogsId,
            @Parameter(description = "커서 ID (다음 페이지 시작점, 첫 요청 시 생략)") @RequestParam(required = false) Long cursorId,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size) {
        var response = reviewService.getReviewsByDiscogsId(discogsId, cursorId, size);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "리뷰 단건 삭제", description = "리뷰 ID로 특정 리뷰를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        reviewService.deleteReview(reviewId, userDetails.getUser());
        return ResponseEntity.ok().build();
    }
}
