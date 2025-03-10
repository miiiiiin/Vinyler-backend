package miiiiiin.com.vinyler.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.VinylDto;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.dto.UserDto;
import miiiiiin.com.vinyler.user.dto.request.ClientRegisterReqeustDto;
import miiiiiin.com.vinyler.user.dto.request.LoginRequestDto;
import miiiiiin.com.vinyler.user.dto.response.LoginResponseDto;
import miiiiiin.com.vinyler.user.dto.response.UserResponseDto;
import miiiiiin.com.vinyler.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원가입 엔드포인트")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(description = "신규 회원가입")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody @Valid ClientRegisterReqeustDto request) {
        var response = userService.registerUser(ServiceRegisterDto.of(request.getEmail(), request.getPassword(), request.getNickname(), request.getProfile(), request.getBirthday()));
        return ResponseEntity.ok(response);
    }

    /**
     * 유저별 찜한 음반 리스트 조회
     */
    @GetMapping("/{userId}/liked")
    @Operation(description = "유저별 찜한 Vinyl 리스트 조회")
    public ResponseEntity<List<VinylDto>> getVinylsLikedByUser(@PathVariable Long userId,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = userService.getVinylsLikedByUser(userId, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    /**
     * 유저별 감상한 음반 리스트 조회
     */
    @GetMapping("/{userId}/listened")
    @Operation(description = "유저별 감상한 Vinyl 리스트 조회")
    public ResponseEntity<List<VinylDto>> getVinylsListenedByUser(@PathVariable Long userId,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = userService.getVinylsListenedByUser(userId, userDetails.getUser());
        return ResponseEntity.ok(response);
    }


    /**
     * 팔로우
     */
    @PostMapping("/{userId}/following")
    @Operation(description = "Follow 기능")
    public ResponseEntity<UserDto> follow(@PathVariable Long userId,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = userService.follow(userId, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    /**
     * 언팔로우
     */
    @DeleteMapping("/{userId}/following")
    @Operation(description = "Follow 기능")
    public ResponseEntity<UserDto> unfollow(@PathVariable Long userId,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = userService.unfollow(userId, userDetails.getUser());
        return ResponseEntity.ok(response);
    }
}
