package miiiiiin.com.vinyler.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.UserVinylStatusDto;
import miiiiiin.com.vinyler.application.dto.VinylDetailDto;
import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.dto.response.PopularVinylDto;
import miiiiiin.com.vinyler.application.service.PopularVinylService;
import miiiiiin.com.vinyler.application.service.UserVinylStatusService;
import miiiiiin.com.vinyler.application.service.VinylService;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vinyls")
@RequiredArgsConstructor
@Tag(name = "Vinyl", description = "Vinyl 찜/감상 관련 엔드포인트")
public class VinylController {

    private final VinylService vinylService;
    private final UserVinylStatusService uservinylStatusService;
    private final PopularVinylService popularVinylService;

    @PostMapping("/likes")
    @Operation(summary = "찜 토글", description = "Vinyl을 찜하거나 찜 취소합니다. 이미 찜한 경우 취소, 아닌 경우 찜 처리됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "찜 토글 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "Vinyl을 찾을 수 없음")
    })
    public VinylLikeDto toggleLike(
            @RequestBody LikeRequestDto request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return vinylService.toggleLike(request, userDetails.getUser());
    }

    @PostMapping("/listen")
    @Operation(summary = "감상 토글", description = "Vinyl의 감상 상태를 토글합니다. 이미 감상한 경우 취소, 아닌 경우 감상 처리됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "감상 토글 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "Vinyl을 찾을 수 없음")
    })
    public UserVinylStatusDto toggleListenStatus(
            @RequestBody LikeRequestDto request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return uservinylStatusService.toggleListenStatus(request, userDetails.getUser());
    }

    @GetMapping("/{discogs_id}")
    @Operation(summary = "Vinyl 상세 메타 조회", description = "특정 Vinyl(Discogs ID 기준)의 DB 상 찜/리뷰 수와 현재 사용자의 찜/감상 상태를 조회합니다. 트랙리스트/이미지 등 Discogs 원본 데이터는 클라이언트가 Discogs API를 직접 호출해 조합합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "Vinyl을 찾을 수 없음")
    })
    public ResponseEntity<VinylDetailDto> getVinylDetail(
            @Parameter(description = "Discogs Release ID", example = "1234567") @PathVariable(name = "discogs_id") Long discogsId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = vinylService.getVinylDetail(discogsId, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/popular")
    @Operation(summary = "인기 음반 랭킹", description = "좋아요 많은 순 상위 N개 음반을 반환합니다.")
    public ResponseEntity<List<PopularVinylDto>> popular(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(popularVinylService.getTop(limit));
    }
}
