package miiiiiin.com.vinyler.discogs.client;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.exception.discogs.DiscogsUnavailableException;
import miiiiiin.com.vinyler.exception.vinyl.VinylNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class DiscogsClient {

    private final RestClient restClient;

    public String getReleaseRaw(Long discogsId) {
        try {
            return restClient.get()
                    .uri("/releases/{id}", discogsId)
                    .retrieve()
                    // discogs가 404 -> 진짜 없는 음반 -> 여기서도 404
                    .onStatus(status -> status.value() == 404,
                            (request, response) -> { throw new VinylNotFoundException(discogsId); })
                    .body(String.class);
            // 응답 JSON 문자열 그대로 받음
        } catch (VinylNotFoundException e) {
            // 404 그대로 통과
            throw e;
        } catch (RestClientException e) {
            // 5xx/429(rate limit) /타임아웃/ 연결 실패 전부 여기로 탐 (503 등) => 모두 RestClientException 계열
            throw new DiscogsUnavailableException();
        }
    }
}
