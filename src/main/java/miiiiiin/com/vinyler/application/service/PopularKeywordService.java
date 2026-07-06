package miiiiiin.com.vinyler.application.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.response.PopularKeywordDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private static final String KEY_PREFIX = "search:keyword:";
    // 시간대(시 단위) 버킷 키 포맷: search:keyword:2026070614
    private static final DateTimeFormatter BUCKET_FMT = DateTimeFormatter.ofPattern("yyyyMMddHH");
    // 버킷 보관 기간(조회 윈도우보다 넉넉히) — 지나면 Redis가 자동 삭제
    private static final Duration BUCKET_TTL = Duration.ofHours(3);

    private String bucketKey(LocalDateTime time) {
        return KEY_PREFIX + time.format(BUCKET_FMT);
    }

    private String normalize(String keyword) {
        if (keyword == null) return null;
        String n = keyword.trim().toLowerCase();
        return n.isBlank() ? null : n;
    }

    /** 검색 1회 발생 시: 현재 시간 버킷에 점수 +1 + 버킷 TTL 갱신 */
    public void record(String keyword) {
        String normalized = normalize(keyword);
        if (normalized == null) return;

        String key = bucketKey(LocalDateTime.now());
        redisTemplate.opsForZSet().incrementScore(key, normalized, 1);
        // 윈도우 밖 버킷 자동 만료
        redisTemplate.expire(key, BUCKET_TTL);
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
