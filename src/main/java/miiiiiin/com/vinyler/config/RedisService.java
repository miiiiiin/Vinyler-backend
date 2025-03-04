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

    /**
     * key(email) : value(refreshToken)
     * @param key
     * @param value
     * @param expTime
     */
    public void setStringValue(String key, String value, Long expTime) {
        ValueOperations<String, Object> stringValueOperations = redisTemplate.opsForValue();
        //  set(key, value, {ttl(expiration time)})
        stringValueOperations.set(key, value, expTime, TimeUnit.MILLISECONDS);
    }

}
