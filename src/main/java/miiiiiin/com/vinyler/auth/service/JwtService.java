package miiiiiin.com.vinyler.auth.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private final SecretKey key;

    // @Value 통해서 yml 파일에 secret-key 값 읽어와서 키 값 초기화
    public JwtService(@Value("${jwt.secret-key}") String key) {
        // secret-key 다시 base64 디코딩하여 토큰 생성 시 필요한 키로 만듬
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
    }

    /*
        subject를 담아 해당 키로 사인함. jwt 토큰 생성
        만료시점: 현재로부터 3시간 뒤
     */
    public String generateToken(String subject) {
        var now = new Date();
        var exp = new Date(now.getTime() + (1000 * 60 * 60 * 3));
        return Jwts.builder().subject(subject).signWith(key)
                .issuedAt(now)
                .expiration(exp)
                .compact();
    }

    /*
        jwt에서 subject 추출
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
            logger.error("JWTException :", e);
            throw e;
        }
    }

    /*
        실제 jwt 인증에서 사용될 함수 (
     */
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername());
    }

    /*
        accessToken으로부터 subject(=username)을 추출하는 함수
     */
    public String getUsername(String accessToken) {
        return getSubject(accessToken);
    }
}
