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
        return tokenDto;
    }

    /**
     * 사용자가 로그아웃한 후에도, Access Token을 다시 사용해서 요청을 보낼 가능성이 있음
     * 그리하여 Redis에서 "logout" 값이 있으면, 해당 토큰은 더 이상 사용할 수 없도록 체크
     * 로그아웃 후 기존 Access Token이 유효해도 사용 불가 (로그아웃된 토큰 차단)
     * 키 : accessToken, 값: "logout"
     * @param accessToken
     */
    public void logout(String accessToken) {
        String username = jwtTokenProvider.getUsername(accessToken);
        // redis에 저장되어있는 refreshToken 삭제
        if (redisService.getValues(username) != null) {
            // 로그아웃 후 더 이상 재발급 요청 불가
            redisService.deleteValues(username);
        }
        // 사용자가 로그아웃했음을 기록하기 위해 Access Token을 Redis에 저장
        long expTime = jwtTokenProvider.getRefreshExpirationTime();
        redisService.setStringValue(accessToken, "logout", expTime);
    }
}
