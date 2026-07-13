package miiiiiin.com.vinyler.discogs.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class DiscogsClient {

    private final RestClient restClient;

    public String getReleaseRaw(Long discogsId) {
        return restClient.get()
                .uri("/release/{id}", discogsId)
                .retrieve()
                .body(String.class);
        // 응답 JSON 문자열 그대로 받음
    }
}
