package miiiiiin.com.vinyler.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.VinylDto;
import miiiiiin.com.vinyler.application.dto.response.SliceResponse;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.dto.UserDto;
import miiiiiin.com.vinyler.user.dto.request.ClientRegisterReqeustDto;
import miiiiiin.com.vinyler.user.dto.request.UpdateUserRequestDto;
import miiiiiin.com.vinyler.user.dto.response.UserResponseDto;
import miiiiiin.com.vinyler.auth.filter.JwtTokenProvider;
import miiiiiin.com.vinyler.auth.service.AuthService;
import miiiiiin.com.vinyler.user.service.UserService;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 관련 엔드포인트")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "신규 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody @Valid ClientRegisterReqeustDto request) {
        var response = userService.registerUser(ServiceRegisterDto.of(request.getEmail(), request.getPassword(), request.getNickname(), request.getProfile(), request.getBirthday()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/liked")
    @Operation(summary = "찜한 Vinyl 목록 조회", description = "특정 유저가 찜한 Vinyl 목록을 커서 기반 페이징으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    public ResponseEntity<SliceResponse<VinylDto>> getVinylsLikedByUser(
            @Parameter(description = "조회할 유저 ID", example = "1") @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "커서 ID (다음 페이지 시작점, 첫 요청 시 생략)") @RequestParam(required = false) Long cursorId,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size) {
        var response = userService.getVinylsLikedByUser(userId, userDetails.getUser(), cursorId, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/listened")
    @Operation(summary = "감상한 Vinyl 목록 조회", description = "특정 유저가 감상한 Vinyl 목록을 커서 기반 페이징으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    public ResponseEntity<SliceResponse<VinylDto>> getVinylsListenedByUser(
            @Parameter(description = "조회할 유저 ID", example = "1") @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "커서 ID (다음 페이지 시작점, 첫 요청 시 생략)") @RequestParam(required = false) Long cursorId,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size) {
        var response = userService.getVinylsListenedByUser(userId, userDetails.getUser(), cursorId, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/following")
    @Operation(summary = "팔로우", description = "특정 유저를 팔로우합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팔로우 성공"),
            @ApiResponse(responseCode = "400", description = "자기 자신을 팔로우할 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 팔로우 중인 유저")
    })
    public ResponseEntity<UserDto> follow(
            @Parameter(description = "팔로우할 유저 ID", example = "2") @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = userService.follow(userId, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/following")
    @Operation(summary = "언팔로우", description = "특정 유저의 팔로우를 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "언팔로우 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "팔로우 관계를 찾을 수 없음")
    })
    public ResponseEntity<UserDto> unfollow(
            @Parameter(description = "언팔로우할 유저 ID", example = "2") @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = userService.unfollow(userId, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/followers")
    @Operation(summary = "팔로워 목록 조회", description = "특정 유저의 팔로워 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    public ResponseEntity<List<UserDto>> getFollowersByUser(
            @Parameter(description = "조회할 유저 ID", example = "1") @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = userService.getFollowersByUser(userId, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/followings")
    @Operation(summary = "팔로잉 목록 조회", description = "특정 유저가 팔로우하는 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    public ResponseEntity<List<UserDto>> getFollowingsByUser(
            @Parameter(description = "조회할 유저 ID", example = "1") @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = userService.getFollowingsByUser(userId, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<UserDto> getUserInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = userService.getUserInfo(userDetails.getUser());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping
    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 닉네임")
    })
    public ResponseEntity<UserDto> updateUserInfo(
            @RequestBody UpdateUserRequestDto request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = userService.updateUserInfo(userDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자를 탈퇴 처리하고 세션을 무효화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<Void> withdrawUser(
            @Parameter(description = "Bearer {accessToken}") @RequestHeader(value = HttpHeaders.AUTHORIZATION) String accessToken,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.withdrawUser(userDetails.getUser());

        if (accessToken.startsWith(JwtTokenProvider.BEARER_PREFIX)) {
            accessToken = accessToken.substring(JwtTokenProvider.BEARER_PREFIX.length());
        }
        authService.logout(accessToken);

        return ResponseEntity.ok().build();
    }
}
