package com.study.platform.global.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class IdempotencyObjectStorageAdapter implements IdempotencyObjectStoragePort {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean setIfAbsent(String key, String marker, Duration ttl) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(key, marker, ttl.toSeconds(), TimeUnit.SECONDS));
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
