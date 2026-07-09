package miiiiiin.com.vinyler.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.event.VinylEventStreamConsumer;
import miiiiiin.com.vinyler.application.event.VinylEventStreamProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class VinylStreamConsumerConfig {

    public static final String GROUP = "vinyl-aggregator";
    private static final String CONSUMER_NAME = "consumer-1";

    private final RedisConnectionFactory connectionFactory;
    private final StringRedisTemplate stringRedisTemplate;
    private final VinylEventStreamConsumer consumer;

    // 컨슈머 그룹 보장 (없으면 생성하고, 이미 있으면 예외 무시)
    @PostConstruct
    public void createGroupIfAbsent() {
        try {
            stringRedisTemplate.opsForStream()
                    .createGroup(VinylEventStreamProducer.STREAM_KEY, GROUP);
        } catch (Exception e) {
            // 이미 그룹이 있으면 BUSYGROUP 예외가 나는데, 정상 상황이라 무시한다
        }
    }

    // 스트림을 백그라운드 스레드로 폴링하는 컨테이너
    @Bean(initMethod = "start", destroyMethod = "stop")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamContainer() {
        // 2초마다 새 메시지 확인하는 옵션
        var options = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions.builder()
                .pollTimeout(Duration.ofSeconds(2))
                .build();

        var container = StreamMessageListenerContainer.create(connectionFactory, options);

        // receive() = 수동 ACK 모드
        container.receive(
                Consumer.from(GROUP, CONSUMER_NAME),
                StreamOffset.create(VinylEventStreamProducer.STREAM_KEY, ReadOffset.lastConsumed()),
                consumer
        );
        return container;
    }
}
