package com.study.platform.global.auth.adapter

import com.study.platform.global.auth.port.TokenRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisTokenRepositoryAdapter(
    private val stringRedisTemplate: StringRedisTemplate
) : TokenRepository {

    override fun save(key: String, value: String, ttl: Duration) {
        stringRedisTemplate.opsForValue().set(key, value, ttl)
    }

    override fun find(key: String): String? =
        stringRedisTemplate.opsForValue().get(key)

    override fun delete(key: String) {
        stringRedisTemplate.delete(key)
    }
}
