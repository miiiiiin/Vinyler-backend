package miiiiiin.com.vinyler.application.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.response.PopularVinylDto;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PopularVinylService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final VinylRepository vinylRepository;
    private static final String KEY = "vinyl:popularity";

    // ZINCRBY 쓰기: 좋아요 발생 시 점수 가감 (+1 좋아요 / -1 취소)
    public void addScore(Long discogsId, double delta) {
        redisTemplate.opsForZSet().incrementScore(KEY, String.valueOf(discogsId), delta);
    }

    public List<PopularVinylDto> getTop(int limit) {
        // Redis에서 상위 id + 점수 (이미 점수 내림차순 정렬됨)
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(KEY, 0, limit - 1);
        if (tuples == null || tuples.isEmpty()) return List.of();

        // id만 모아 DB 한 번에 조회 -> discogsId로 빠르게 찾도록 Map 구성
        List<Long> ids = new ArrayList<>();
        for (var t : tuples) ids.add(Long.valueOf(String.valueOf(t.getValue())));

        Map<Long, Vinyl> vinylMap = vinylRepository.findByDiscogsIdIn(ids).stream()
                .collect(Collectors.toMap(Vinyl::getDiscogsId, v -> v));

        // 순위 순서는 "Redis tuples" 기준으로 돌면서 제목 채움
        List<PopularVinylDto> result = new ArrayList<>();

        long rank = 1;
        for (ZSetOperations.TypedTuple<Object> t : tuples) {
            Long discogsId = Long.valueOf(String.valueOf(t.getValue()));
            long score = t.getScore() == null ? 0 : t.getScore().longValue();
            Vinyl vinyl = vinylMap.get(discogsId);
            result.add(new PopularVinylDto(rank++, discogsId, score,
                    vinyl != null ? vinyl.getTitle() : null,
                    vinyl != null ? vinyl.getArtistsSort() : null));
        }

        return  result;
    }
}
