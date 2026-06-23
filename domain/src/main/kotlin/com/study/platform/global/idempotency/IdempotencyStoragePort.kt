package com.study.platform.global.idempotency

import java.time.Duration

interface IdempotencyStoragePort {
    fun get(key: String): String?
    fun setIfAbsent(key: String, value: String, ttl: Duration): Boolean
    fun set(key: String, value: String, ttl: Duration)
    fun delete(key: String)
}
