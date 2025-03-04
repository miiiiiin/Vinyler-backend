package miiiiiin.com.vinyler.config;

import jakarta.transaction.Transactional;
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
    @Transactional
    public void setStringValue(String key, String value, Long expTime) {
        ValueOperations<String, Object> stringValueOperations = redisTemplate.opsForValue();
        //  set(key, value, {ttl(expiration time)})
        stringValueOperations.set(key, value, expTime, TimeUnit.MILLISECONDS);
    }

    public String getValues(String key) {
        return String.valueOf(redisTemplate.opsForValue().get(key));
    }
}
