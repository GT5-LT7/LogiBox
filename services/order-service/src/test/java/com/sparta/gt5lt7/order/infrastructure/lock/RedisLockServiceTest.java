package com.sparta.gt5lt7.order.infrastructure.lock;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RedisLockServiceTest {

    @Test
    void Redis_락_획득_성공() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent("lock:test", "value", Duration.ofSeconds(5)))
                .thenReturn(true);

        RedisLockService redisLockService = new RedisLockService(redisTemplate);

        boolean result = redisLockService.tryLock(
                "lock:test",
                "value",
                Duration.ofSeconds(5)
        );

        assertThat(result).isTrue();
    }
}