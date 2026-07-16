package miiiiiin.com.vinyler.application.event;

import miiiiiin.com.vinyler.application.service.PopularVinylService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VinylEventStreamConsumerTest {

    @Mock
    PopularVinylService popularVinylService;
    @Mock
    StringRedisTemplate stringRedisTemplate;
    @Mock
    ValueOperations<String, String> valueOps;
    @Mock
    StreamOperations<String, Object, Object> streamOps;
    @InjectMocks
    VinylEventStreamConsumer consumer;

    private MapRecord<String, String, String> record(String eventId) {
        Map<String, String> body = Map.of(
                "eventId", eventId, "type", "LIKE", "discogsId", "12345", "delta", "1");
        return StreamRecords.mapBacked(body)
                .withStreamKey("stream:vinyl-events")
                .withId(RecordId.of("1-0"));
    }


//    @Test
//    @DisplayName("같은 eventId 이벤트가 두 번 와도 집계는 한 번만")
//    void duplicateEvent_aggregatesOnce() {
//        given(stringRedisTemplate.opsForValue()).willReturn(valueOps);
//        given(stringRedisTemplate.opsForStream()).willReturn(streamOps);
//        // 처음 = true(처음 봄), 두 번째 = false(중복)
//        given(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
//                .willReturn(true, false);
//
//        var msg = record("evt-1");
//        consumer.onMessage(msg);   // 1회차
//        consumer.onMessage(msg);   // 2회차(중복)
//
//        //  addScore는 1번, acknowledge는 2번. 즉 "중복은 skip하되 pending엔 안 남긴다"
//        verify(popularVinylService, times(1)).addScore(12345L, 1.0); // 집계는 딱 1번
//        verify(streamOps, times(2)).acknowledge(anyString(), anyString(), (String) any()); // ACK는 둘 다
//    }
}