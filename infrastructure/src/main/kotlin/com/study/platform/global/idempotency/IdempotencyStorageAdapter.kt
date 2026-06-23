package com.study.platform.global.idempotency

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class IdempotencyStorageAdapter(
    private val stringRedisTemplate: StringRedisTemplate
) : IdempotencyStoragePort {

    override fun get(key: String): String? =
        stringRedisTemplate.opsForValue().get(key)

    override fun setIfAbsent(key: String, value: String, ttl: Duration): Boolean =
        stringRedisTemplate.opsForValue().setIfAbsent(key, value, ttl) == true

    override fun set(key: String, value: String, ttl: Duration) {
        stringRedisTemplate.opsForValue().set(key, value, ttl)
    }

    override fun delete(key: String) {
        stringRedisTemplate.delete(key)
    }
}
