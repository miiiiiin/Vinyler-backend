package miiiiiin.com.vinyler.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.auth.repository.RefreshTokenRepository;
import miiiiiin.com.vinyler.global.GlobalResponseDto;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// spring security 체인에 등록할 커스텀 필터
@Component
@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 요청이 해당 필터를 거치면 실행되는 메소드
     * 클라이언트는 토큰값을 헤더에 담아 전송해야함.
     * 토큰은 앞에 Bearer를 붙여서 전달해야 함
     * 토큰 값 검증 (토큰 유효기간 만료 여부 검증)
     * 토큰 검증 완료되었으면 SecurityContextHolder에 context, authenticationToken 세팅
     *
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
        String BEARER_PREFIX = JwtTokenProvider.BEARER_PREFIX;

        String accessTokenFromHeader = jwtTokenProvider.getHeaderAccessToken(request);
        String refreshTokenFromHeader = jwtTokenProvider.getHeaderRefreshToken(request);

//        var authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
//        var refresh = jwtTokenProvider.getHeaderRefreshToken(request);
        var securityContext = SecurityContextHolder.getContext();

        if (accessTokenFromHeader != null) {
            // 액세스토큰 값이 유효하면 setAuthentication 통해 securityContext에 인증 정보 저장
            if (jwtTokenProvider.validateToken(accessTokenFromHeader)) {
                var username = jwtTokenProvider.getUsername(accessTokenFromHeader);
                var userDetails = userService.loadUserByUsername(username);
                setAuthentication((UserDetailsImpl) userDetails, request, securityContext);
            } else if (refreshTokenFromHeader != null) {
                // 액세스 토큰이 만료되어있으나 리프레쉬 토큰이 존재하는 상황
                // 리프레쉬 토큰 검증 && 리프레쉬 토큰 DB에서 토큰 존재유무 확인
                boolean isRefreshToken = validateRefreshToken(refreshTokenFromHeader);
                // 리프레쉬 토큰이 유효하고 DB에 있는 것과 비교했을 때 같으면
                if (isRefreshToken) {
                    String username = jwtTokenProvider.getSubject(refreshTokenFromHeader);
//                    // 새로운 액세스 토큰 발급
                    var userDetails = userService.loadUserByUsername(username);
                    var newAccessToken = jwtTokenProvider.generateAccessToken((UserDetailsImpl) userDetails);
                    // 헤더에 액세스 토큰 추가
                    jwtTokenProvider.setHeaderAccessToken(response, newAccessToken.getAccessToken());
                    // Security context에 인증 정보 넣기
                    setAuthentication((UserDetailsImpl) userDetails, request, securityContext);
                }

            } else {
                // 리프레쉬 토큰 만료되었거나 DB에 있는 것과 같지 않다면
                jwtExceptionHandler(response, "RefreshToken Expired", HttpStatus.BAD_REQUEST);
                return;
            }
        }

//
//        if (!ObjectUtils.isEmpty(authorization) &&
//            authorization.startsWith(BEARER_PREFIX) &&
//            securityContext.getAuthentication() == null) {
//
//            var accessToken = authorization.substring(BEARER_PREFIX.length());
//
//            // 토큰 만료 검증
//            if (!jwtTokenProvider.validateToken(accessToken)) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//            var username = jwtTokenProvider.getUsername(accessToken);
//            var userDetails = userService.loadUserByUsername(username);
//
//            var authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//            // Security Filter에 의한 사용자 권한별 인가 처리에 사용됨
//            SecurityContextHolder.setContext(securityContext);
//            securityContext.setAuthentication(authenticationToken);
//        }

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
     * -> db에 저장되어 있는 token과 비교
     * -> db에 저장한다는 것이 jwt token을 사용한다는 강점을 상쇄시킨다.
     * -> db 보다는 redis를 사용하는 것이 더욱 좋다. (in-memory db기 때문에 조회속도가 빠르고 주기적으로 삭제하는 기능이 기본적으로 존재합니다.)
     */
    private boolean validateRefreshToken(String jwtToken) {
        if (!jwtTokenProvider.validateToken(jwtToken)) return false;
        // FIX: fix later
        // DB에 저장한 토큰 비교
        var username = jwtTokenProvider.getUsername(jwtToken);
        var refreshToken = refreshTokenRepository.findByEmail(username);
        return refreshToken.isPresent() && jwtToken.equals(refreshToken.get().getRefreshToken());
    }

    // JWT 예외처리
    public void jwtExceptionHandler(HttpServletResponse response, String message, HttpStatus status) {
        response.setStatus(status.value());
//        response.setContentType("application/json");
        try {
            String json = new ObjectMapper().writeValueAsString(HttpStatus.valueOf(status.value()));
            response.getWriter().write(json);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }
}
