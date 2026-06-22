package com.study.platform.support.fake

import com.study.platform.global.auth.port.TokenRepository
import java.time.Duration

class FakeTokenRepository : TokenRepository {

    private val store: MutableMap<String, String> = HashMap()

    override fun save(key: String, value: String, ttl: Duration) {
        store[key] = value
    }

    override fun find(key: String): String? = store[key]

    override fun delete(key: String) {
        store.remove(key)
    }
}
