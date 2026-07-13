package miiiiiin.com.vinyler.discogs.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class DiscogsClientConfig {

    private final DiscogsProperties props;

    @Bean
    public RestClient discogsRestClient() {
        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader(HttpHeaders.USER_AGENT, props.userAgent())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Discogs token=" + props.token())
                .build();
    }
}
