package miiiiiin.com.vinyler.application.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.response.PopularKeywordDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PopularKeywordService {

    /**
     * 기존 RedisTemplate 빈을 그대로 재사용
     * 키/값 StringRedisSerializer라 ZSET 멤버 문자열이 그대로 저장됨)
     */
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY = "popular:keywords";

    // 검색 1회 발생 시 점수 +1 (원자적으로)
    public void record(String keyword) {
        if (keyword==null) return;
        String normalized = keyword.trim().toLowerCase();
        if (normalized.isBlank()) return;
        redisTemplate.opsForZSet().incrementScore(KEY, normalized, 1);
    }

    // 상위 limit개를 점수 내림차순으로 조회
    public List<PopularKeywordDto> getTopKeywords(int limit) {
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(KEY, 0, limit - 1);
        List<PopularKeywordDto> result = new ArrayList<>();
        if (tuples==null) return result;

        long rank = 1;
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            String keyword = String.valueOf(tuple.getValue());
            long count = tuple.getScore() == null ? 0 : tuple.getScore().longValue();
            result.add(new PopularKeywordDto(rank++, keyword, count));
        }
        return result;
    }
}
