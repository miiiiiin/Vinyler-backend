package miiiiiin.com.vinyler.config;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.auth.filter.CustomUsernamePasswordAuthenticationFilter;
import miiiiiin.com.vinyler.auth.filter.JwtTokenProvider;
import miiiiiin.com.vinyler.auth.filter.JwtVerificationFilter;
import miiiiiin.com.vinyler.user.service.SocialOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtVerificationFilter jwtVerificationFilter;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final RedisService redisService;


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        /**
         * UrlBasedCorsConfigurationSource : configuration을 특정 url 패턴에서만 적용할 수 있게 함
         */
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    private final SocialOAuth2UserService socialOAuth2UserService;

    // AuthenticationManager의 Bean을 얻기 위한 authConfiguration 객체
    private final AuthenticationConfiguration authenticationConfiguration;
    /**
     * AuthenticationConfiguration로부터 AuthenticationManager 객체 가져오는 메서드
     * @param authenticationConfiguration
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider) throws Exception {

        // 커스텀 필터 등록
        // 로그인 경로 설정 후, 로그인 필터 등록
        CustomUsernamePasswordAuthenticationFilter filter = new CustomUsernamePasswordAuthenticationFilter(authenticationManager(authenticationConfiguration), jwtTokenProvider, redisService);
        filter.setFilterProcessesUrl("/api/*/auth/login"); //  로그인 필터가 작동될 경로 설정


        http
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests((requests) ->
                            requests
                                    .requestMatchers(HttpMethod.POST, "/api/*/user/register", "/api/*/auth/login", "/api/v1/auth/reissue")
                                    .permitAll()
                                    .anyRequest()
                                    .authenticated()
                        )
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable) //  CSRF 공격 방어 임시 해제
//                .oauth2Login(oauth -> oauth
//                        .userInfoEndpoint(userInfo -> userInfo
//                                .userService(socialOAuth2UserService)));

                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtVerificationFilter, CustomUsernamePasswordAuthenticationFilter.class) // jwt 검증 필터 등록
                .addFilterAfter(jwtExceptionFilter, jwtVerificationFilter.getClass())
                .httpBasic(HttpBasicConfigurer::disable) // 기본 로그인창 disable
                .formLogin(FormLoginConfigurer::disable); // UsernamePasswordAuthenticationFilter disable



        return http.build();
    }
}
