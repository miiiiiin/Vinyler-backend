package miiiiiin.com.vinyler.auth.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import miiiiiin.com.vinyler.auth.dto.TokenInfoDto;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final SecretKey key;

    public static final String REFRESH = "Refresh";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION = "Authorization";

    // 토큰 유효시간(3시간)
    @Value("${jwt.token.access-expiration-time}")
    private long accessExpirationTime;
    // refreshToken 유효시간 7일
    @Value("${jwt.token.refresh-expiration-time}") @Getter
    private long refreshExpirationTime;

    // @Value 통해서 yml 파일에 secret-key 값 읽어와서 키 값 초기화
//    public JwtTokenProvider(@Value("${jwt.secret-key}") String key) {
//        // secret-key 다시 base64 디코딩하여 토큰 생성 시 필요한 키로 만듬
//        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
//    }

    public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey) {
        // Keys.hmacShaKeyFor()가 일반 바이트 배열도 받을 수 있음. (jwt 0.12.6) Base64 디코딩 작업 불필요. 라이브러리 내부에서 자동처리
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
        subject를 담아 해당 키로 사인함. jwt 토큰 생성
        만료시점: 현재로부터 3시간 뒤
     */
    private TokenInfoDto generateToken(String subject) {
        Instant now = Instant.now();
        Instant accessExpireAt = now.plusMillis(accessExpirationTime);
        Instant refreshExpireAt = now.plusMillis(refreshExpirationTime);

        String accessToken = Jwts.builder().subject(subject)
                .signWith(key)
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessExpireAt))
                .compact();

        String refreshToken = Jwts.builder().subject(subject)
                .signWith(key)
                .issuedAt(Date.from(now))
                .expiration(Date.from(refreshExpireAt))
                .compact();

        return TokenInfoDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
        jwt 토큰 복호화하여 subject 추출 (username)
     */
    public String getSubject(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException e) {
            logger.error("JWT Exception :", e);
            throw e;
        }
    }

    /**
     * 실제 jwt 인증에서 사용될 함수 (
     */
    public TokenInfoDto generateAccessToken(UserDetailsImpl userDetails) {
        return generateToken(userDetails.getUsername());
    }

    /**
     * accessToken으로부터 subject(=username)을 추출하는 함수
     */
    public String getUsername(String accessToken) {
        return getSubject(accessToken);
    }

    /**
     * 토큰의 유효성과 만료 여부 확인
     * 이미 만료된 토큰에서 페이로드를 파싱하는 과정에서 에러가 발생하기 때문에 예외 처리
     */
    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(jwtToken);
            logger.info("JWT Expiration :", claims.getPayload().getExpiration());
            // exp 날짜가 현재 날짜보다 전에 있지 않으면 토큰 만료
            return !claims.getPayload().getExpiration().before(new Date());
        } catch (Exception e) {
            logger.error("JWT Exception :", e);
            return false;
        }
    }

    /**
     * 토큰의 유효성과 만료일자 확인
     * refreshToken 토큰 검증
     * db에 저장된 토큰 불러오는 대신 redis에서 저장된 토큰을 불러와서 비교하는 것으로 수정
     * -> db 보다는 redis를 사용하는 것이 더욱 좋다. (in-memory db기 때문에 조회속도가 빠르고 주기적으로 삭제하는 기능이 기본적으로 존재)
     */
    public boolean validateRefreshToken(String refreshToken, String redisRefreshToken) {
        if (!validateToken(refreshToken)) return false;
        return !refreshToken.isEmpty() && refreshToken.equals(redisRefreshToken);
    }

    // 액세스 토큰 헤더 설정
    public void setHeaderAccessToken(HttpServletResponse response, String accessToken) {
        response.setHeader(AUTHORIZATION, BEARER_PREFIX + accessToken);
    }

//    // 리프레쉬 토큰 헤더 설정
//    public void setHeaderRefreshToken(HttpServletResponse response, String refreshToken) {
//        response.setHeader(REFRESH, refreshToken);
//    }

    // 헤더 accessToken 리턴
    public String getHeaderAccessToken(HttpServletRequest request) {
        String accessToken = request.getHeader(AUTHORIZATION);
        if (accessToken != null && accessToken.startsWith(BEARER_PREFIX)) {
            return accessToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    // 헤더 refreshToken 리턴
//    public String getHeaderRefreshToken(HttpServletRequest request) {
//        String refreshToken = request.getHeader(REFRESH);
//        if (refreshToken != null) {
//            return refreshToken;
//        }
//        return null;
//    }

    public Long getRefreshExpirationTime() {
        return refreshExpirationTime;
    }
}
