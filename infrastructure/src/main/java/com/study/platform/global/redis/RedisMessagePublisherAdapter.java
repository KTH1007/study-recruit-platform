package com.study.platform.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisMessagePublisherAdapter implements RedisMessagePublisher {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void publish(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }
}
