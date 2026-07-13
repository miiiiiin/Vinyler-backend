package miiiiiin.com.vinyler.config.discogs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discogs")
public record DiscogsProperties(String baseUrl,
                                String token,
                                String userAgent) {
}
