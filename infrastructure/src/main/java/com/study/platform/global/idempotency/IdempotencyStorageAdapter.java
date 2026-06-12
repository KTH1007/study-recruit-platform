package com.study.platform.global.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class IdempotencyStorageAdapter implements IdempotencyStoragePort {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean setIfAbsent(String key, String value, Duration ttl) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, value, ttl));
    }

    @Override
    public void set(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }
}
