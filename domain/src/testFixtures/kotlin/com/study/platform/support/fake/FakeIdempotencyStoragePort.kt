package com.study.platform.support.fake

import com.study.platform.global.idempotency.IdempotencyStoragePort
import java.time.Duration

class FakeIdempotencyStoragePort : IdempotencyStoragePort {

    private val store: MutableMap<String, String> = HashMap()

    override fun get(key: String): String? = store[key]

    override fun setIfAbsent(key: String, value: String, ttl: Duration): Boolean {
        if (store.containsKey(key)) {
            return false
        }
        store[key] = value
        return true
    }

    override fun set(key: String, value: String, ttl: Duration) {
        store[key] = value
    }

    override fun delete(key: String) {
        store.remove(key)
    }
}
