package miiiiiin.com.vinyler.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import org.antlr.v4.runtime.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

//@Service
//@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final SecretKey key;

    // 토큰 유효시간(3시간)
    private long tokenValidityInSeconds = 1000 * 60 * 60 * 3;
    // refreshToken 유효시간 30일
    private long refreshTokenValidityInSeconds = 30 * 24 * 60 * 60 * 1000L;

    // @Value 통해서 yml 파일에 secret-key 값 읽어와서 키 값 초기화
    public JwtTokenProvider(@Value("${jwt.secret-key}") String key) {
        // secret-key 다시 base64 디코딩하여 토큰 생성 시 필요한 키로 만듬
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
    }

    /**
        subject를 담아 해당 키로 사인함. jwt 토큰 생성
        만료시점: 현재로부터 3시간 뒤
     */
    private TokenInfoDto generateToken(String subject) {
        var now = new Date();
        var accessExp = new Date(now.getTime() + tokenValidityInSeconds);
        var refreshExp = new Date(now.getTime() + refreshTokenValidityInSeconds);

        String accessToken = Jwts.builder().subject(subject)
                .signWith(key)
                .issuedAt(now)
                .expiration(accessExp)
                .compact();

        String refreshToken = Jwts.builder().subject(subject)
                .signWith(key)
                .issuedAt(now)
                .expiration(refreshExp)
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

    /*
        실제 jwt 인증에서 사용될 함수 (
     */
    public TokenInfoDto generateAccessToken(UserDetailsImpl userDetails) {
        return generateToken(userDetails.getUsername());
    }

    /*
        accessToken으로부터 subject(=username)을 추출하는 함수
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
     */
    public boolean validateRefreshToken(String jwtToken) {
        if (!validateToken(jwtToken)) return false;
        // FIX: fix later
        return true;
    }


}
