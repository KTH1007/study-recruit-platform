package com.study.platform.global.ratelimit

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component

@Component
class RateLimitStorageAdapter(
    private val stringRedisTemplate: StringRedisTemplate
) : RateLimitStoragePort {

    companion object {
        private val SLIDING_WINDOW_SCRIPT: RedisScript<Long> = RedisScript.of(
            """
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
            """.trimIndent(),
            Long::class.java
        )
    }

    override fun isAllowed(key: String, windowSeconds: Long, limit: Long): Boolean {
        val result = stringRedisTemplate.execute(
            SLIDING_WINDOW_SCRIPT,
            listOf(key),
            System.currentTimeMillis().toString(),
            windowSeconds.toString(),
            limit.toString()
        )
        return result == 1L
    }
}
