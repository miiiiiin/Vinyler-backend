package miiiiiin.com.vinyler.application.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.service.VinylService;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vinyls")
@RequiredArgsConstructor
@Tag(name = "Vinyl", description = "Vinyl 정보 조회 관련 엔드포인트")
public class VinylController {
    private final VinylService vinylService;
    private final UserService userService;

    @PostMapping("/likes")
    public VinylLikeDto toggleLike(@RequestBody LikeRequestDto request,
                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return vinylService.toggleLike(request, userDetails.getUser());
    }
}
