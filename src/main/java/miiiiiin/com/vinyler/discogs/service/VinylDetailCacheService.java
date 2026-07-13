package miiiiiin.com.vinyler.discogs.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.discogs.client.DiscogsClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class VinylDetailCacheService {

    private final StringRedisTemplate redisTemplate;
    private final DiscogsClient discogsClient;

    /** Redis 캐시 레이어 적용 (Key: discogs:release:{discogsId}, TTL: 10~30분) */
    private static final String KEY_PREFIX = "discogs:release:";
    private static final Duration TTL = Duration.ofHours(5); // 라이선스 상한 시간은 6시간이지만, 5시간으로 고정

    public String getReleaseDetail(Long discogsId) {
        String key = KEY_PREFIX + discogsId;

        // 캐시 먼저 확인
        String cached = redisTemplate.opsForValue().get(key);
        // HIT -> API 안부름
        if (cached != null) return cached;

        // MISS -> API 호출
        String releaseRaw = discogsClient.getReleaseRaw(discogsId);
        // Redis에 저장 (TTL 5시간)
        redisTemplate.opsForValue().set(key, releaseRaw, TTL);
        return releaseRaw;
    }
}
