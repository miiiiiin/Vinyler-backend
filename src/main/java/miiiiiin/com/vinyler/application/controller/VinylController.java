package miiiiiin.com.vinyler.application.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.UserVinylStatusDto;
import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.service.UserVinylStatusService;
import miiiiiin.com.vinyler.application.service.VinylService;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vinyls")
@RequiredArgsConstructor
@Tag(name = "Vinyl", description = "Vinyl 정보 조회 관련 엔드포인트")
public class VinylController {
    private final VinylService vinylService;
    private final UserVinylStatusService uservinylStatusService;

    @PostMapping("/likes")
    public VinylLikeDto toggleLike(@RequestBody LikeRequestDto request,
                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return vinylService.toggleLike(request, userDetails.getUser());
    }

    @PostMapping("/listen")
    public UserVinylStatusDto toggleListenStatus(@RequestBody LikeRequestDto request,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return uservinylStatusService.toggleListenStatus(request, userDetails.getUser());
    }

//    @PostMapping
//    public ResponseEntity<Vinyl> createVinyl(@RequestBody LikeRequestDto request,
//                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
////        return vinylService.toggleLike(request, userDetails.getUser());
//        var vinyl = vinylService.createVinyl(request, userDetails.getUser());
//        return ResponseEntity.ok(vinyl);
//    }
}
