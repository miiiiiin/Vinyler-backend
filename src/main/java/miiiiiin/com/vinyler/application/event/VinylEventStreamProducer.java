package miiiiiin.com.vinyler.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewChanged(ReviewChangedEvent e) {
        publish("REVIEW", e.eventId(), e.discogsId(), e.delta());
    }

    /** 이벤트를 스트림 한 줄로 적재 (XADD)
     *
     * stream:vinyl-events라는 스트림에
     * {type, discogsId, delta} 같은 필드를 한 줄 적재(XADD)만 하고 끝ㅊ
     *
     * Stream 쓰는 이유
     * 이미 @TransactionalEventListener + @Async로 스프링 이벤트를 처리하고 있는데, 그건 같은 애플리케이션 프로세스 안에서만 동작함
     * 그 인메모리 이벤트 대신 Redis Stream에 한 번 더 쌓아두면
     * 1. 컨슈머가 죽었다 살아나도 처리 못 한 이벤트를 그대로 이어받을 수 있고
     * 2. Postgres 트랜잭션과 별개로, "이벤트가 실제로 발생했다"는 사실 자체를 영속적인 로그로 남길 수 있음
     * 3. 인스턴스가 여러 대여도 이벤트를 공유할 수 있음
     *
     *  즉, 인메모리 이벤트 -> (같은 프로세스 안에서 즉시 소비) 방식의 한계(프로세스 재시작 시 유실, 다중 인스턴스 확장 불가)를
     *  Redis Stream이 보완해주는 구조
     *
     */
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
