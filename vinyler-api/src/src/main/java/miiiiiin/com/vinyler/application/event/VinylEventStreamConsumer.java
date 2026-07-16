package miiiiiin.com.vinyler.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miiiiiin.com.vinyler.application.service.PopularVinylService;
import miiiiiin.com.vinyler.config.VinylStreamConsumerConfig;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VinylEventStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final PopularVinylService popularVinylService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final Duration DEDUP_TTL = Duration.ofDays(1);

    // 스트림에 새 메시지가 오면 1건씩 여기로 들어옴
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        Map<String, String> body = message.getValue();
        String eventId = body.get("eventId");
        Long discogsId = Long.valueOf(body.get("discogsId"));
        int delta = Integer.parseInt(body.get("delta"));

        // 멱등성 : processed:{eventId} 도장을 원자적으로 찍음 (SET NX)
        String dedupKey = "processed:" + eventId;
        // 새로 찍히면 true(=내가 처음) / 이미 있으면 false(=누가 이미 처리함)
        Boolean first = stringRedisTemplate.opsForValue()
                .setIfAbsent(dedupKey, "1", DEDUP_TTL);

        if (Boolean.TRUE.equals(first)) {
            // 처음 보는 이벤트면 => 실제 집계
            // 실제 집계: ZINCRBY vinyl:popularity {delta} {discogsId}
            popularVinylService.addScore(discogsId, delta);
            log.info("[EVENT STREAM] processed id={} discogsId={} delta={}",
                    message.getId(), discogsId, delta);
        } else {
            // 이미 처리한 이벤트 => 집계 SKIP (중복)
            log.info("[EVENT STREAM] skip duplicate eventId={}", eventId);
        }

        // 처리 완료 -> ACK (pending 목록에서 제거)
        stringRedisTemplate.opsForStream().acknowledge(
                VinylEventStreamProducer.STREAM_KEY,
                VinylStreamConsumerConfig.GROUP,
                message.getId()
        );
    }
}


