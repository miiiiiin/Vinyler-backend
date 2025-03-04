package miiiiiin.com.vinyler.auth.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.auth.dto.TokenInfoDto;
import miiiiiin.com.vinyler.config.RedisService;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.dto.request.LoginRequestDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 클라이언트의 로그인 요청을 처리해주는 필터를 커스텀한 클래스
 */
@RequiredArgsConstructor
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    private ObjectMapper objectMapper;

    /**
     * 로그인 요청이 들어올때 인증 과정을 처리하는 메소드
     * request에서 inputStream을 얻은 후, inputStream을 통해 requestBody값을 문자열로 넘겨받음. LoginRequestDto로 객체로 파싱하여 토큰 생성
     * 이후, 생성한 토큰 인증 객체를 AuthenticationManager에게 넘겨주어 인증 과정을 위임
     * @param request from which to extract parameters and perform the authentication
     * @param response the response, which may be needed if the implementation has to do a
     * redirect as part of a multi-stage authentication process (such as OIDC).
     * @return
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        // request body GET
        objectMapper = new ObjectMapper();
        ServletInputStream servletInputStream;
        String requestBody;

        try {
            servletInputStream = request.getInputStream();
            requestBody = StreamUtils.copyToString(servletInputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Json data parsing
        LoginRequestDto loginDto;
        try {
            loginDto = objectMapper.readValue(requestBody, LoginRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());
        return authenticationManager.authenticate(authToken);
    }

    /**
     * 인증이 성공적으로 완료되면 실행되는 메소드
     * @param request
     * @param response
     * @param chain
     * @param authResult the object returned from the <tt>attemptAuthentication</tt>
     * method.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // 토큰 생성
        UserDetailsImpl userDetails = (UserDetailsImpl) authResult.getPrincipal();
        TokenInfoDto tokenDto = jwtTokenProvider.generateAccessToken(userDetails);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();

        // 헤더에 액세스 토큰 추가
        jwtTokenProvider.setHeaderAccessToken(response, accessToken);
        jwtTokenProvider.setHeaderRefreshToken(response, refreshToken);

        objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(tokenDto);

        // redis에 refreshToken 저장
        redisService.setStringValue(userDetails.getUsername(), refreshToken, jwtTokenProvider.getRefreshExpirationTime());

        // JSON 타입 객체 응답
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);
        this.getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
    }
}
