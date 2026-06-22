package com.study.platform.global.ratelimit

interface RateLimitStoragePort {
    fun isAllowed(key: String, windowSeconds: Long, limit: Long): Boolean
}
