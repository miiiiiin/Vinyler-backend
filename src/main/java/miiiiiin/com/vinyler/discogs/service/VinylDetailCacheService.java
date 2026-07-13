package miiiiiin.com.vinyler.discogs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.response.DiscogsReleaseDto;
import miiiiiin.com.vinyler.discogs.client.DiscogsClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class VinylDetailCacheService {

    private final StringRedisTemplate redisTemplate;
    private final DiscogsClient discogsClient;
    private final ObjectMapper objectMapper;

    /** Redis 캐시 레이어 적용 (Key: discogs:release:{discogsId}, TTL: 10~30분) */
    private static final String KEY_PREFIX = "discogs:release:";
    private static final Duration TTL = Duration.ofHours(5); // 라이선스 상한 시간은 6시간이지만, 5시간으로 고정


    public DiscogsReleaseDto getReleaseDetail(Long discogsId) {
        String key = KEY_PREFIX + discogsId;

        // 캐시 먼저 확인
        // HIT -> API 안부름
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            // MISS -> API 호출 + redis 저장
            json = discogsClient.getReleaseRaw(discogsId);

            // Redis에 저장 (TTL 5시간)
            redisTemplate.opsForValue().set(key, json, TTL);
        }
        // 캐시엔 여전히 "문자열"로 저장하고, 파싱은 꺼낼 때
        return parse(json);
    }

    public DiscogsReleaseDto parse(String json) {
        try {
            return objectMapper.readValue(json, DiscogsReleaseDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Discogs 응답 파싱 실패", e);
        }
    }
}
