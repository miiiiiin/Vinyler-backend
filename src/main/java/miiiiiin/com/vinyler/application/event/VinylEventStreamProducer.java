package miiiiiin.com.vinyler.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VinylEventStreamProducer {

    private final StringRedisTemplate stringRedisTemplate;

    public static final String STREAM_KEY = "stream:vinyl-events";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLiked(VinylLikedEvent e) {
        publish("LIKE", e.eventId(), e.discogsId(), e.delta());
    }

    @EventListener
    public void onReviewChanged(ReviewChangedEvent e) {
        publish("REVIEW", e.eventId(), e.discogsId(), e.delta());
    }

    // 이벤트를 스트림 한 줄로 적재 (XADD)
    private void publish(String type, String eventId, Long discogsId, int delta) {
        Map<String, String> fields = Map.of(
                "eventId", eventId,
                "type", type, // LIKE/REVIEW
                "discogsId", String.valueOf(discogsId),
                "delta", String.valueOf(delta)
        );

        var recordId = stringRedisTemplate.opsForStream().add(STREAM_KEY, fields);
        log.info("[STREAM] XADD {} id={} type={} discogsId={} delta={}",
                STREAM_KEY, recordId, type, discogsId, delta);
    }
}
