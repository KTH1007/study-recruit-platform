package com.study.platform.global.auth.adapter;

import com.study.platform.global.auth.port.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisTokenRepositoryAdapter implements TokenRepository {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public Optional<String> find(String key) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }
}
