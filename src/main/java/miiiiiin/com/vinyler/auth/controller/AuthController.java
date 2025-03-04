package miiiiiin.com.vinyler.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.response.DefaultResponseDto;
import miiiiiin.com.vinyler.auth.dto.TokenInfoDto;
import miiiiiin.com.vinyler.auth.filter.JwtTokenProvider;
import miiiiiin.com.vinyler.auth.service.AuthService;
import miiiiiin.com.vinyler.global.GlobalResponseDto;
import miiiiiin.com.vinyler.user.dto.request.LoginRequestDto;
import miiiiiin.com.vinyler.user.dto.response.LoginResponseDto;
import org.apache.coyote.Response;
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
    @Operation(description = "로그인")
    public void login(@RequestBody @Valid LoginRequestDto request) {
    }

    /// reissue: validate 요청으로부터 UNAUTHORIZED(401)을 반환받았다면,
    // 프론트에서 Cookie와 Header에 각각 RT와 AT를 요청으로 받아서 authService.reissue를 통해 토큰 재발급을 진행한다.
    // 토큰 재발급이 성공한다면 login과 마찬가지로 응답 결과를 보내고,
    // 토큰 재발급이 실패했을때(null을 반환받았을 때) Cookie에 담긴 RT를 삭제하고 재로그인을 유도한다.

    /**
     * 로그인 시에는 쿠키에 refreshToken을 저장했고, JwtVerificationFilter에서도 refreshToken이 유효하다면(액세스 토큰 만료되었을 경우)
     * 새로운 액세스 토큰을 재발급하도록 처리하였으나 api도 따로 마련함
     * @param accessToken
     * @param refreshToken
     * @return
     */

    @PatchMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String accessToken,
                                     @CookieValue("refreshToken") String refreshToken) {

        var tokenDto = authService.reissue(accessToken, refreshToken);
        // 토큰 재발급이 완료되었을 경우
        if (tokenDto != null) {

            // 리프레쉬 토큰 쿠키 저장
            HttpCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenDto.getRefreshToken())
                    .httpOnly(true) // 자바스크립트에서 접근 불가능
                    .secure(true) // HTTPS에서만 전송 가능
                    .sameSite("Strict") // CSRF 공격 방지
                    .path("/") // 전체 도메인에서 사용 가능
                    .maxAge(jwtTokenProvider.getRefreshExpirationTime()) // 유효 시간 설정
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
    public ResponseEntity<?> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String accessToken) {
        if (accessToken.startsWith(JwtTokenProvider.BEARER_PREFIX)) {
            accessToken = accessToken.substring(JwtTokenProvider.BEARER_PREFIX.length());
        }
        authService.logout(accessToken);
        // 로그아웃 시, 클라이언트가 가지고 있는 리프레쉬 토큰 제거
        // 해당 리프레쉬 토큰을 사용해 새 액세스 토큰을 발급받을 수 있는 보안 문제 상황 방지

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resetCookie().toString())
                .build();
    }

    private HttpCookie resetCookie() {
        return ResponseCookie.from("refreshToken", "")
                .maxAge(0) // 쿠키 만료 설정
                .path("/")
                .build();
    }
}
