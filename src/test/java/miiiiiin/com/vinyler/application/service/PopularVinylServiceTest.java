package miiiiiin.com.vinyler.application.service;

import miiiiiin.com.vinyler.application.dto.response.PopularVinylDto;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PopularVinylServiceTest {
    @Mock
    RedisTemplate<String, Object> redisTemplate;
    @Mock
    ZSetOperations<String, Object> zSetOps;
    @Mock
    VinylRepository vinylRepository;
    @InjectMocks
    PopularVinylService popularVinylService;

    @Test
    @DisplayName("랭킹 순서는 Redis 기준, 제목은 DB에서 채운다")
    void getTop_orderFromRedis_titleFromDb() {
        given(redisTemplate.opsForZSet()).willReturn(zSetOps);

        // Redis 순위: 100(2점) > 200(1점)  ※ 순서 유지되는 LinkedHashSet
        Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("100", 2.0));
        tuples.add(new DefaultTypedTuple<>("200", 1.0));
        given(zSetOps.reverseRangeWithScores("vinyl:popularity", 0, 9)).willReturn(tuples);

        // DB는 일부러 반대 순서로 반환 (IN 결과 순서 보장 X 상황 재현)
        given(vinylRepository.findByDiscogsIdIn(anyList())).willReturn(List.of(
                Vinyl.builder().discogsId(200L).title("B").artistsSort("artB").build(),
                Vinyl.builder().discogsId(100L).title("A").artistsSort("artA").build()));

        var result = popularVinylService.getTop(10);

        // 순서는 Redis 기준(100 → 200)
        assertThat(result).extracting(PopularVinylDto::discogsId).containsExactly(100L, 200L);
        // 제목은 id에 맞게 정확히 매칭
        assertThat(result.get(0).title()).isEqualTo("A");
        assertThat(result.get(0).rank()).isEqualTo(1L);
    }
}