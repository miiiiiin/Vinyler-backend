package miiiiiin.com.vinyler.discogs.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class DiscogsClientConfig {

    private final DiscogsProperties props;

    @Bean
    public RestClient discogsRestClient() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));

        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(factory) // 3초 안에 응답없으면 타임아웃 (RestClientException -> 503)
                .defaultHeader(HttpHeaders.USER_AGENT, props.userAgent())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Discogs token=" + props.token())
                .build();
    }
}
