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
import java.util.UUID;

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

    /**
     * 최근 windowHours시간 버킷을 ZUNIONSTORE로 합쳐 상위 limit개 반환
     * windowHours=2 이면 "지금 시간 + 직전 시간" 버킷 합산 = 실시간 랭킹
     */
    public List<PopularKeywordDto> getTopKeywords(int limit, int windowHours) {
        LocalDateTime now = LocalDateTime.now();

        // 최근 windowHours개의 버킷 키 수집 (현재시간부터 과거)
        List<String> keys = new ArrayList<>();
        for (int i=0; i<windowHours; i++) {
            keys.add(bucketKey(now.minusHours(i)));
        }

        // 합친 결과 담을 임시 키(호출마다 유니크 -> 동시 요청 간섭 방지)
        String destKey = KEY_PREFIX + "union:" + UUID.randomUUID();
        try {
            // keys[0]을 기준으로 나머지 버킷 union (같은 멤버 점수 합산)하여 destkey에 저장
            redisTemplate.opsForZSet().unionAndStore(
                    keys.get(0),
                    keys.subList(1, keys.size()),
                    destKey
            );

            Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(destKey, 0, limit - 1);
            List<PopularKeywordDto> result = new ArrayList<>();
            if (tuples==null) return result;

            long rank = 1;
            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                String keyword = String.valueOf(tuple.getValue());
                long count = tuple.getScore() == null ? 0 : tuple.getScore().longValue();
                result.add(new PopularKeywordDto(rank++, keyword, count));
            }
            return result;
        } finally {
            // 임시 키 정리
            redisTemplate.delete(destKey);
        }
    }
}
