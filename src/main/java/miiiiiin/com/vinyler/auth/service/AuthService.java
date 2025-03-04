package miiiiiin.com.vinyler.auth.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.auth.dto.TokenInfoDto;
import miiiiiin.com.vinyler.auth.filter.JwtTokenProvider;
import miiiiiin.com.vinyler.config.RedisService;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public TokenInfoDto reissue(String accessToken, String refreshToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String refreshTokenInRedis = redisService.getValues(userDetails.getUsername());
        if (refreshTokenInRedis == null) {
            // redis에 refreshToken가 없는 경우, 재로그인 요청
            return null;
        }

        // refreshToken 유효성 검사, redis에 저장된 토큰과 같은지를 비교 (같지 않으면 삭제 및 재로그인 요청)
        if (!jwtTokenProvider.validateRefreshToken(refreshToken, refreshTokenInRedis)) {
            redisService.deleteValues(userDetails.getUsername());
            return null;
        }

        // 액세스 토큰 재발급 및 redis 갱신
        redisService.deleteValues(userDetails.getUsername());
        var tokenDto = jwtTokenProvider.generateAccessToken(userDetails);
        redisService.setStringValue(userDetails.getUsername(), refreshToken, jwtTokenProvider.getRefreshExpirationTime());

        // 리프레쉬 토큰 쿠키 저장
        HttpCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenDto.getRefreshToken())
                .httpOnly(true) // 자바스크립트에서 접근 불가능
                .secure(true) // HTTPS에서만 전송 가능
                .sameSite("Strict") // CSRF 공격 방지
                .path("/") // 전체 도메인에서 사용 가능
                .maxAge(jwtTokenProvider.getRefreshExpirationTime()) // 유효 시간 설정
                .build();

        return tokenDto;
    }
}
