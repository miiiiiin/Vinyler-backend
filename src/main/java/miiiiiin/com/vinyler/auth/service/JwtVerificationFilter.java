package miiiiiin.com.vinyler.auth.service;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import miiiiiin.com.vinyler.user.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// spring security 체인에 등록할 커스텀 필터
@Component
public class JwtVerificationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;
    private Filter filter;

    /**
     * 요청이 해당 필터를 거치면 실행되는 메소드
     * 클라이언트는 토큰값을 헤더에 담아 전송해야함.
     * 토큰은 앞에 Bearer를 붙여서 전달해야 함
     * 토큰 값 검증 (토큰 유효기간 만료 여부 검증)
     * 토큰 검증 완료되었으면 SecurityContextHolder에 context, authenticationToken 세팅
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String BEARER_PREFIX = "Bearer ";
        var authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        var securityContext = SecurityContextHolder.getContext();

        if (!ObjectUtils.isEmpty(authorization) &&
            authorization.startsWith(BEARER_PREFIX) &&
            securityContext.getAuthentication() == null) {

            var accessToken = authorization.substring(BEARER_PREFIX.length());

            // 토큰 만료 검증
            if (!jwtTokenProvider.validateToken(accessToken)) {
                filterChain.doFilter(request, response);
                return;
            }
            var username = jwtTokenProvider.getUsername(accessToken);
            var userDetails = userService.loadUserByUsername(username);

            var authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // Security Filter에 의한 사용자 권한별 인가 처리에 사용됨
            SecurityContextHolder.setContext(securityContext);
            securityContext.setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}
