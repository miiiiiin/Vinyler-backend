package miiiiiin.com.vinyler.config;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.auth.service.JwtAuthenticationFilter;
import miiiiiin.com.vinyler.user.service.SocialOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtExceptionFilter jwtExceptionFilter;
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
        source.registerCorsConfiguration("/api/v1/**", configuration);
        return source;
    }


    private final SocialOAuth2UserService socialOAuth2UserService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests((requests) ->
                            requests
                                    .requestMatchers(HttpMethod.POST, "/api/*/user/register", "/api/*/user/login")
                                    .permitAll()
                                    .anyRequest()
                                    .authenticated()
                        )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, jwtAuthenticationFilter.getClass())
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(socialOAuth2UserService)));
        return http.build();
    }
}
