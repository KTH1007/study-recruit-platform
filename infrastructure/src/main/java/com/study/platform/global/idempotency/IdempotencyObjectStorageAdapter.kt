package com.study.platform.global.idempotency

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
class IdempotencyObjectStorageAdapter(
    private val redisTemplate: RedisTemplate<String, Any>
) : IdempotencyObjectStoragePort {

    override fun get(key: String): Any? =
        redisTemplate.opsForValue().get(key)

    override fun setIfAbsent(key: String, marker: String, ttl: Duration): Boolean =
        redisTemplate.opsForValue().setIfAbsent(key, marker, ttl.toSeconds(), TimeUnit.SECONDS) == true

    override fun set(key: String, value: Any, ttl: Duration) {
        redisTemplate.opsForValue().set(key, value, ttl)
    }

    override fun delete(key: String) {
        redisTemplate.delete(key)
    }
}
