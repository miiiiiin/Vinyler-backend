package miiiiiin.com.vinyler.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.IncorrectClaimException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.config.RedisService;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

// spring security 체인에 등록할 커스텀 필터
@Component
@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RedisService redisService;

    /**
     * 요청이 해당 필터를 거치면 실행되는 메소드
     * 클라이언트는 토큰값을 헤더에 담아 전송해야함.
     * 토큰은 앞에 Bearer를 붙여서 전달해야 함
     * 토큰 값 검증 (토큰 유효기간 만료 여부 검증)
     * 토큰 검증 완료되었으면 SecurityContextHolder에 context, authenticationToken 세팅
     * <p>
     * 클라이언트가 header에 토큰값을 실어보내면 doFilterInternal 안에서 토큰 검증 실행
     * 인증 객체 생성 후, Security Context에 정보 저장
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        var securityContext = SecurityContextHolder.getContext();


        String accessToken = jwtTokenProvider.getHeaderAccessToken(request);

        // 쿠키에서 Refresh Token 가져오기
        String refreshTokenFromCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

//        // 특정 API 경로에서는 필터를 건너뜀
//        if (requestURI.contains("/auth/reissue") || requestURI.contains("/auth/logout")) {
//            filterChain.doFilter(request, response);
//            return;
//        }

        try {
            // 유효한 토큰인지 검사
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                var username = jwtTokenProvider.getUsername(accessToken);
                var userDetails = userService.loadUserByUsername(username);
                // 액세스토큰 값이 유효하면 setAuthentication 통해 securityContext에 인증 정보 저장
                setAuthentication((UserDetailsImpl) userDetails, request, securityContext);
            } else if (refreshTokenFromCookie != null) {
                // 액세스 토큰이 만료되어있으나 리프레쉬 토큰이 존재하는 상황
                // 리프레쉬 토큰 검증 && 리프레쉬 토큰 쿠키에서 토큰 존재유무 확인
                boolean isRefreshToken = validateRefreshToken(refreshTokenFromCookie);
                // 리프레쉬 토큰이 유효하고 쿠키에 있는 것과 비교했을 때 같으면
                if (isRefreshToken) {
                    String username = jwtTokenProvider.getSubject(refreshTokenFromCookie);
                    // 새로운 액세스 토큰 발급
                    var userDetails = userService.loadUserByUsername(username);
                    var newAccessToken = jwtTokenProvider.generateAccessToken((UserDetailsImpl) userDetails);
                    // 헤더에 액세스 토큰 추가
                    jwtTokenProvider.setHeaderAccessToken(response, newAccessToken.getAccessToken());
                    // Security context에 인증 정보 넣기
                    setAuthentication((UserDetailsImpl) userDetails, request, securityContext);
                } else {
                    // 리프레쉬 토큰 만료되었거나 쿠키에 있는 것과 같지 않다면
                    jwtExceptionHandler(response, "RefreshToken Expired", HttpStatus.BAD_REQUEST);
                    return;
                }
            }
        } catch (IncorrectClaimException e) {
            // 잘못된 토큰일 경우
            SecurityContextHolder.clearContext();
            logger.debug("Invalid JWT token.");
            response.sendError(403);
        } catch (UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            logger.debug("Cannot find user.");
            response.sendError(403);
        }

        filterChain.doFilter(request, response);
    }

    public void setAuthentication(UserDetailsImpl userDetails, HttpServletRequest request, SecurityContext securityContext) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        securityContext.setAuthentication(authenticationToken);
    }

    /**
     * 토큰의 유효성과 만료일자 확인
     * refreshToken 토큰 검증
     * db에 저장된 토큰 불러오는 대신 redis에서 저장된 토큰을 불러와서 비교하는 것으로 수정
     * -> db 보다는 redis를 사용하는 것이 더욱 좋다. (in-memory db기 때문에 조회속도가 빠르고 주기적으로 삭제하는 기능이 기본적으로 존재)
     */
    private boolean validateRefreshToken(String jwtToken) {
        var username = jwtTokenProvider.getUsername(jwtToken);
        var refreshToken = redisService.getValues(username);
        return jwtTokenProvider.validateRefreshToken(jwtToken, refreshToken);
    }

    // JWT 예외처리
    public void jwtExceptionHandler(HttpServletResponse response, String message, HttpStatus status) {
        response.setStatus(status.value());
        response.setContentType("application/json");
        try {
            String json = new ObjectMapper().writeValueAsString(HttpStatus.valueOf(status.value()) + " " + message);
            response.getWriter().write(json);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
