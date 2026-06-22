package com.study.platform.global.idempotency

import java.time.Duration

interface IdempotencyObjectStoragePort {
    fun get(key: String): Any?
    fun setIfAbsent(key: String, marker: String, ttl: Duration): Boolean
    fun set(key: String, value: Any, ttl: Duration)
    fun delete(key: String)
}
