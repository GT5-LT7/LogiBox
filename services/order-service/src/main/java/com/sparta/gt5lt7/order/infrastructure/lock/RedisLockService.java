package com.sparta.gt5lt7.order.infrastructure.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisLockService {

    private final StringRedisTemplate redisTemplate;

    public boolean tryLock(String key, String value, Duration ttl) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
        return Boolean.TRUE.equals(result);
    }

    public void unlock(String key, String value) {
        String currentValue = redisTemplate.opsForValue().get(key);

        if (value.equals(currentValue)) {
            redisTemplate.delete(key);
        }
    }
}
