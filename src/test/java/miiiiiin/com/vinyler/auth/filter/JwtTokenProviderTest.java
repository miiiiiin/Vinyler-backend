package miiiiiin.com.vinyler.auth.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import miiiiiin.com.vinyler.auth.dto.TokenInfoDto;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider 단위 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private User testUser;
    private UserDetailsImpl userDetails;
    private String secretKey = "dmlueWxlci1zY2FuLWl0LXJpZ2h0YXdheQo=";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessExpirationTime", 10800000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpirationTime", 604800000L);

        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("testuser")
                .password("password123")
                .build();

        userDetails = new UserDetailsImpl(testUser);
    }

    @Test
    @DisplayName("액세스 토큰 생성 - 성공")
    void generateAccessToken_Success() {
        // When
        TokenInfoDto result = jwtTokenProvider.generateAccessToken(userDetails);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGrantType()).isEqualTo("Bearer");
        assertThat(result.getAccessToken()).isNotNull();
        assertThat(result.getRefreshToken()).isNotNull();
    }

    @Test
    @DisplayName("토큰에서 사용자명 추출 - 성공")
    void getUsername_Success() {
        // Given
        TokenInfoDto tokenDto = jwtTokenProvider.generateAccessToken(userDetails);

        // When
        String username = jwtTokenProvider.getUsername(tokenDto.getAccessToken());

        // Then
        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 유효한 토큰")
    void validateToken_ValidToken_ReturnsTrue() {
        // Given
        TokenInfoDto tokenDto = jwtTokenProvider.generateAccessToken(userDetails);

        // When
        boolean isValid = jwtTokenProvider.validateToken(tokenDto.getAccessToken());

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 잘못된 토큰")
    void validateToken_InvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("리프레시 토큰 검증 - 성공")
    void validateRefreshToken_ValidToken_ReturnsTrue() {
        // Given
        TokenInfoDto tokenDto = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenDto.getRefreshToken();

        // When
        boolean isValid = jwtTokenProvider.validateRefreshToken(refreshToken, refreshToken);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("리프레시 토큰 검증 - 토큰이 일치하지 않음")
    void validateRefreshToken_TokenMismatch_ReturnsFalse() {
        // Given
        TokenInfoDto tokenDto = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenDto.getRefreshToken();
        String differentToken = "different.refresh.token";

        // When
        boolean isValid = jwtTokenProvider.validateRefreshToken(refreshToken, differentToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("리프레시 토큰 검증 - 빈 Redis 토큰")
    void validateRefreshToken_EmptyRedisToken_ReturnsFalse() {
        // Given
        TokenInfoDto tokenDto = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenDto.getRefreshToken();

        // When
        boolean isValid = jwtTokenProvider.validateRefreshToken(refreshToken, "");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("헤더에 액세스 토큰 설정")
    void setHeaderAccessToken_Success() {
        // Given
        String accessToken = "test.access.token";

        // When
        jwtTokenProvider.setHeaderAccessToken(response, accessToken);

        // Then
        verify(response, times(1)).setHeader("Authorization", "Bearer test.access.token");
    }

    @Test
    @DisplayName("헤더에서 액세스 토큰 가져오기 - 성공")
    void getHeaderAccessToken_WithBearerPrefix_Success() {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer test.access.token");

        // When
        String token = jwtTokenProvider.getHeaderAccessToken(request);

        // Then
        assertThat(token).isEqualTo("test.access.token");
    }

    @Test
    @DisplayName("헤더에서 액세스 토큰 가져오기 - Bearer 접두사 없음")
    void getHeaderAccessToken_NoBearerPrefix_ReturnsNull() {
        // Given
        when(request.getHeader("Authorization")).thenReturn("test.access.token");

        // When
        String token = jwtTokenProvider.getHeaderAccessToken(request);

        // Then
        assertThat(token).isNull();
    }

    @Test
    @DisplayName("헤더에서 액세스 토큰 가져오기 - 헤더 없음")
    void getHeaderAccessToken_NoHeader_ReturnsNull() {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        String token = jwtTokenProvider.getHeaderAccessToken(request);

        // Then
        assertThat(token).isNull();
    }

    @Test
    @DisplayName("토큰 만료 시간 가져오기")
    void getAccessTokenExpirationTime_ReturnsCorrectValue() {
        // When
        Long expirationTime = jwtTokenProvider.getAccessTokenExpirationTime();

        // Then
        assertThat(expirationTime).isEqualTo(10800000L);
    }

    @Test
    @DisplayName("리프레시 토큰 만료 시간 가져오기")
    void getRefreshExpirationTime_ReturnsCorrectValue() {
        // When
        Long expirationTime = jwtTokenProvider.getRefreshExpirationTime();

        // Then
        assertThat(expirationTime).isEqualTo(604800000L);
    }

    @Test
    @DisplayName("토큰에서 Subject 추출 - 성공")
    void getSubject_Success() {
        // Given
        TokenInfoDto tokenDto = jwtTokenProvider.generateAccessToken(userDetails);

        // When
        String subject = jwtTokenProvider.getSubject(tokenDto.getAccessToken());

        // Then
        assertThat(subject).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("토큰에서 Subject 추출 - 잘못된 토큰으로 예외 발생")
    void getSubject_InvalidToken_ThrowsException() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.getSubject(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("생성된 토큰이 정상적으로 파싱됨")
    void generatedToken_CanBeParsed() {
        // Given
        TokenInfoDto tokenDto = jwtTokenProvider.generateAccessToken(userDetails);

        // When
        String username = jwtTokenProvider.getUsername(tokenDto.getAccessToken());
        boolean isValid = jwtTokenProvider.validateToken(tokenDto.getAccessToken());

        // Then
        assertThat(username).isEqualTo("test@example.com");
        assertThat(isValid).isTrue();
    }
}