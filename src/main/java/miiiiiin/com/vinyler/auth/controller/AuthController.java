package miiiiiin.com.vinyler.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.auth.filter.JwtTokenProvider;
import miiiiiin.com.vinyler.auth.service.AuthService;
import miiiiiin.com.vinyler.user.dto.request.LoginRequestDto;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증/로그인")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인합니다. 성공 시 Access Token(헤더)과 Refresh Token(HttpOnly 쿠키)을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 값"),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    public void login(@RequestBody @Valid LoginRequestDto request) {
    }

    @PatchMapping("/reissue")
    @Operation(summary = "토큰 재발급", description = "만료된 Access Token을 Refresh Token을 이용해 재발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token — 재로그인 필요")
    })
    public ResponseEntity<?> reissue(
            @Parameter(description = "Bearer {accessToken}", example = "Bearer eyJhbGci...")
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String accessToken,
            @Parameter(description = "HttpOnly 쿠키로 전달되는 Refresh Token")
            @CookieValue("refreshToken") String refreshToken) {

        var tokenDto = authService.reissue(accessToken, refreshToken);
        if (tokenDto != null) {
            HttpCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenDto.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(jwtTokenProvider.getRefreshExpirationTime())
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, resetCookie().toString())
                    .build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Redis에서 Refresh Token을 삭제하고 쿠키를 초기화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Access Token")
    })
    public ResponseEntity<?> logout(
            @Parameter(description = "Bearer {accessToken}", example = "Bearer eyJhbGci...")
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String accessToken) {

        if (accessToken.startsWith(JwtTokenProvider.BEARER_PREFIX)) {
            accessToken = accessToken.substring(JwtTokenProvider.BEARER_PREFIX.length());
        }
        authService.logout(accessToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resetCookie().toString())
                .build();
    }

    private HttpCookie resetCookie() {
        return ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .path("/")
                .build();
    }
}
