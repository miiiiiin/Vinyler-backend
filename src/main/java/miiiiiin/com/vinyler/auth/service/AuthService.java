package miiiiiin.com.vinyler.auth.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.auth.entity.RefreshToken;
import miiiiiin.com.vinyler.auth.filter.JwtTokenProvider;
import miiiiiin.com.vinyler.auth.repository.RefreshTokenRepository;
import miiiiiin.com.vinyler.exception.user.UserNotFoundException;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.dto.request.LoginRequestDto;
import miiiiiin.com.vinyler.user.dto.response.LoginResponseDto;
import miiiiiin.com.vinyler.user.entity.User;
import miiiiiin.com.vinyler.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private User getUserEntity(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    public LoginResponseDto login(LoginRequestDto requestBody) {
        var userEntity = getUserEntity(requestBody.getEmail());

        if (passwordEncoder.matches(requestBody.getPassword(), userEntity.getPassword())) {
            var tokenDto = jwtTokenProvider.generateAccessToken(UserDetailsImpl.from(userEntity));
            var accessToken = tokenDto.getAccessToken();
            var refreshToken = refreshTokenRepository.findByEmail(userEntity.getEmail());

            if (refreshToken.isPresent()) {
                // 있으면 토큰 업데이트 및 발급 후 DB 저장
                var newToken = refreshToken.get().updateToken(tokenDto.getRefreshToken());
                refreshTokenRepository.save(newToken);
            } else {
                // 없으면 토큰 새로 만들고 DB 저장
                var newToken = new RefreshToken(requestBody.getEmail(), tokenDto.getRefreshToken());
                refreshTokenRepository.save(newToken);
            }

            return new LoginResponseDto(accessToken);
        } else {
            throw new UserNotFoundException();
        }
    }
}
