package miiiiiin.com.vinyler.discogs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discogs")
public record DiscogsProperties(String baseUrl,
                                String token,
                                String userAgent) {
    // 설정값 매핑 프로퍼티 클래스 (yml 설정값 담음)
}
