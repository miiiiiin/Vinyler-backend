package miiiiiin.com.vinyler.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void setStringValue(String token, String data, Long expTime) {
        ValueOperations<String, Object> stringValueOperations = redisTemplate.opsForValue();
        //  set(key, value, {ttl(expiration time)})
        stringValueOperations.set(token, data, expTime, TimeUnit.MILLISECONDS);
    }

}
