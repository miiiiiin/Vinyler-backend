package miiiiiin.com.vinyler.auth.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.auth.entity.RefreshToken;
import miiiiiin.com.vinyler.auth.filter.JwtTokenProvider;
import miiiiiin.com.vinyler.auth.repository.RefreshTokenRepository;
import miiiiiin.com.vinyler.config.RedisService;
import miiiiiin.com.vinyler.exception.user.UserNotFoundException;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.dto.request.LoginRequestDto;
import miiiiiin.com.vinyler.user.dto.response.LoginResponseDto;
import miiiiiin.com.vinyler.user.entity.User;
import miiiiiin.com.vinyler.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    private User getUserEntity(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    public LoginResponseDto login(LoginRequestDto requestBody) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestBody.getEmail(), requestBody.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

//        var userEntity = getUserEntity(requestBody.getEmail());
//        if (passwordEncoder.matches(requestBody.getPassword(), userDetails.getPassword())) {
////            var accessToken = jwtTokenProvider.generateAccessToken(UserDetailsImpl.from(userEntity));
//            return new LoginResponseDto(accessToken.getAccessToken());
//        } else {
//            throw new UserNotFoundException();
//        }


//
        if (passwordEncoder.matches(requestBody.getPassword(), userDetails.getPassword())) {
        // 액세스 토큰 & 리프레시 토큰 생성
            var tokenDto = jwtTokenProvider.generateAccessToken(userDetails);
            var accessToken = tokenDto.getAccessToken();
            var refreshToken = tokenDto.getRefreshToken();

            // Redis에 refreshToken 저장
            redisService.setStringValue(userDetails.getUsername(), refreshToken, jwtTokenProvider.getRefreshExpirationTime());

//
//            if (refreshToken.isPresent()) {
//                // 있으면 토큰 업데이트 및 발급 후 DB 저장
//                var newToken = refreshToken.get().updateToken(tokenDto.getRefreshToken());
//                refreshTokenRepository.save(newToken);
//            } else {
//                // 없으면 토큰 새로 만들고 DB 저장
//                var newToken = new RefreshToken(requestBody.getEmail(), tokenDto.getRefreshToken());
//                refreshTokenRepository.save(newToken);
//            }

            return new LoginResponseDto(accessToken, refreshToken);
        } else {
            throw new UserNotFoundException();
        }
    }
}
