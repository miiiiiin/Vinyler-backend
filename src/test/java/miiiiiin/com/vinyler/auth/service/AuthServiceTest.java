package miiiiiin.com.vinyler.auth.service;

import miiiiiin.com.vinyler.auth.dto.TokenInfoDto;
import miiiiiin.com.vinyler.auth.filter.JwtTokenProvider;
import miiiiiin.com.vinyler.config.RedisService;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisService redisService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserDetailsImpl userDetails;
    private String accessToken;
    private String refreshToken;
    private TokenInfoDto tokenInfoDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("testuser")
                .password("password123")
                .build();

        userDetails = new UserDetailsImpl(testUser);
        accessToken = "valid.access.token";
        refreshToken = "valid.refresh.token";
        
        tokenInfoDto = TokenInfoDto.builder()
                .grantType("Bearer")
                .accessToken("new.access.token")
                .refreshToken("new.refresh.token")
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("토큰 재발급 - 성공")
    void reissue_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(redisService.getValues("test@example.com")).thenReturn(refreshToken);
        when(jwtTokenProvider.validateRefreshToken(refreshToken, refreshToken)).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn(tokenInfoDto);
        when(jwtTokenProvider.getRefreshExpirationTime()).thenReturn(604800000L);

        // When
        TokenInfoDto result = authService.reissue(accessToken, refreshToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new.access.token");
        assertThat(result.getRefreshToken()).isEqualTo("new.refresh.token");
        verify(redisService, times(1)).deleteValues("test@example.com");
        verify(redisService, times(1)).setStringValue(eq("test@example.com"), eq("new.refresh.token"), anyLong());
    }

    @Test
    @DisplayName("토큰 재발급 - Redis에 refreshToken이 없는 경우 null 반환")
    void reissue_NoRefreshTokenInRedis_ReturnsNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(redisService.getValues("test@example.com")).thenReturn(null);

        // When
        TokenInfoDto result = authService.reissue(accessToken, refreshToken);

        // Then
        assertThat(result).isNull();
        verify(jwtTokenProvider, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("토큰 재발급 - refreshToken 유효성 검증 실패 시 null 반환")
    void reissue_InvalidRefreshToken_ReturnsNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(redisService.getValues("test@example.com")).thenReturn("different.refresh.token");
        when(jwtTokenProvider.validateRefreshToken(refreshToken, "different.refresh.token")).thenReturn(false);

        // When
        TokenInfoDto result = authService.reissue(accessToken, refreshToken);

        // Then
        assertThat(result).isNull();
        verify(redisService, times(1)).deleteValues("test@example.com");
        verify(jwtTokenProvider, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    void logout_Success() {
        // Given
        when(jwtTokenProvider.getUsername(accessToken)).thenReturn("test@example.com");
        when(redisService.getValues("test@example.com")).thenReturn(refreshToken);
        when(jwtTokenProvider.getRefreshExpirationTime()).thenReturn(604800000L);

        // When
        authService.logout(accessToken);

        // Then
        verify(redisService, times(1)).deleteValues("test@example.com");
        verify(redisService, times(1)).setStringValue(eq(accessToken), eq("logout"), anyLong());
    }

    @Test
    @DisplayName("로그아웃 - Redis에 refreshToken이 없는 경우에도 accessToken은 저장")
    void logout_NoRefreshTokenInRedis_StillSavesAccessToken() {
        // Given
        when(jwtTokenProvider.getUsername(accessToken)).thenReturn("test@example.com");
        when(redisService.getValues("test@example.com")).thenReturn(null);
        when(jwtTokenProvider.getRefreshExpirationTime()).thenReturn(604800000L);

        // When
        authService.logout(accessToken);

        // Then
        verify(redisService, never()).deleteValues("test@example.com");
        verify(redisService, times(1)).setStringValue(eq(accessToken), eq("logout"), anyLong());
    }

    @Test
    @DisplayName("로그아웃 - accessToken을 logout으로 표시하여 재사용 방지")
    void logout_MarksAccessTokenAsLogout() {
        // Given
        String testAccessToken = "test.access.token";
        when(jwtTokenProvider.getUsername(testAccessToken)).thenReturn("test@example.com");
        when(redisService.getValues("test@example.com")).thenReturn(refreshToken);
        when(jwtTokenProvider.getRefreshExpirationTime()).thenReturn(604800000L);

        // When
        authService.logout(testAccessToken);

        // Then
        verify(redisService).setStringValue(testAccessToken, "logout", 604800000L);
    }
}