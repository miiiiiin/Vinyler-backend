package miiiiiin.com.vinyler.discogs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import miiiiiin.com.vinyler.discogs.client.DiscogsClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class VinylDetailCacheServiceTest {

    @Mock
    StringRedisTemplate redisTemplate;
    @Mock
    ValueOperations<String, String> valueOps;
    @Mock
    DiscogsClient discogsClient;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks VinylDetailCacheService cacheService;

    private static final String KEY = "discogs:release:123";
    private static final String JSON = "{\"id\":123,\"title\":\"OK Computer\"}";


    @Test
    @DisplayName("캐시 HIT면 Discogs를 부르지 않는다")
    void 캐시가_존재하면_DISCOGS를_부르지_않는다() {
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get(KEY)).willReturn(JSON); // 캐시에 있음

        cacheService.getReleaseDetail(123L);

        then(discogsClient).should(never()).getReleaseRaw(anyLong());
    }


    @Test
    @DisplayName("캐시 MISS면 Discogs 1회 호출 후 TTL 5시간으로 저장한다")
    void 캐시_없으면_DISCOGS_1회_호출_후_TTL_제한() {
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        // 캐시에 없음
        given(valueOps.get(KEY)).willReturn(null);
        given(discogsClient.getReleaseRaw(123L)).willReturn(JSON);

        cacheService.getReleaseDetail(123L);

        // 1번만 호출
        then(discogsClient).should(times(1)).getReleaseRaw(123L);
        // TTL 저장
        then(valueOps).should().set(KEY, JSON, Duration.ofHours(5));
    }
}