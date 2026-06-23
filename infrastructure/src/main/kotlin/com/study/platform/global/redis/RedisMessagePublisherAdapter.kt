package com.study.platform.global.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisMessagePublisherAdapter(
    private val stringRedisTemplate: StringRedisTemplate
) : RedisMessagePublisher {

    override fun publish(channel: String, message: String) {
        stringRedisTemplate.convertAndSend(channel, message)
    }
}
