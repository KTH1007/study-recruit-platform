package com.study.platform.global.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RateLimitStorageAdapter implements RateLimitStoragePort {

    private final StringRedisTemplate stringRedisTemplate;

    private static final RedisScript<Long> SLIDING_WINDOW_SCRIPT = RedisScript.of("""
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])
            local clearBefore = now - window * 1000
            redis.call('ZREMRANGEBYSCORE', key, '-inf', clearBefore)
            local count = redis.call('ZCARD', key)
            if count < limit then
                redis.call('ZADD', key, now, now)
                redis.call('PEXPIRE', key, window * 1000)
                return 1
            end
            return 0
            """, Long.class);

    @Override
    public boolean isAllowed(String key, long windowSeconds, long limit) {
        Long result = stringRedisTemplate.execute(
                SLIDING_WINDOW_SCRIPT,
                List.of(key),
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(windowSeconds),
                String.valueOf(limit)
        );
        return Long.valueOf(1L).equals(result);
    }
}
