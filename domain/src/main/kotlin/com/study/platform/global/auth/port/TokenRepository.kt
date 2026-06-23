package com.study.platform.global.auth.port

import java.time.Duration

interface TokenRepository {
    fun save(key: String, value: String, ttl: Duration)
    fun find(key: String): String?
    fun delete(key: String)
}
