package miiiiiin.com.vinyler.application.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.response.PopularVinylDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PopularVinylService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY = "vinyl:popularity";

    // ZINCRBY 쓰기: 좋아요 발생 시 점수 가감 (+1 좋아요 / -1 취소)
    public void addScore(Long discogsId, double delta) {
        redisTemplate.opsForZSet().incrementScore(KEY, String.valueOf(discogsId), delta);
    }

    public List<PopularVinylDto> getTop(int limit) {
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(KEY, 0, limit - 1);

        List<PopularVinylDto> result = new ArrayList<>();
        if (tuples == null) return result;

        long rank = 1;
        for (ZSetOperations.TypedTuple<Object> t : tuples) {
            Long discogsId = Long.valueOf(String.valueOf(t.getValue()));
            long score = t.getScore() == null ? 0 : t.getScore().longValue();
            result.add(new PopularVinylDto(rank++, discogsId, score));
        }

        return  result;
    }
}
