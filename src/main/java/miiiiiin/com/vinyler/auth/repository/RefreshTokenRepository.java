package miiiiiin.com.vinyler.auth.repository;

import jakarta.transaction.Transactional;
import miiiiiin.com.vinyler.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // DB에서 유저 정보로 Refresh Token을 찾기위한 findByEmail을 정의
    Optional<RefreshToken> findByEmail(String email);
    boolean existsByRefreshToken(String token);

    @Transactional
    void deleteByRefreshToken(String refreshToken);
}
