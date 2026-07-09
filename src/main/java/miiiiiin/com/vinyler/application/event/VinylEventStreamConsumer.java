package miiiiiin.com.vinyler.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miiiiiin.com.vinyler.application.service.PopularVinylService;
import miiiiiin.com.vinyler.config.VinylStreamConsumerConfig;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VinylEventStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final PopularVinylService popularVinylService;
    private final StringRedisTemplate stringRedisTemplate;

    // 스트림에 새 메시지가 오면 1건씩 여기로 들어옴
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        Map<String, String> body = message.getValue();
        Long discogsId = Long.valueOf(body.get("discogsId"));
        int delta = Integer.parseInt(body.get("delta"));

        // 실제 집계: ZINCRBY vinyl:popularity {delta} {discogsId}
        popularVinylService.addScore(discogsId, delta);

        // 처리 완료 -> ACK (pending 목록에서 제거)
        stringRedisTemplate.opsForStream().acknowledge(
                VinylEventStreamProducer.STREAM_KEY,
                VinylStreamConsumerConfig.GROUP,
                message.getId()
        );

        log.info("[EVENT STREAM] processed id={} discogsId={} delta={}",
                message.getId(), discogsId, delta);
    }
}


