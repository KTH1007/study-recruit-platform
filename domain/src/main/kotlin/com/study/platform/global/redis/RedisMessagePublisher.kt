package com.study.platform.global.redis

interface RedisMessagePublisher {
    fun publish(channel: String, message: String)
}
