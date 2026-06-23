package com.study.platform.support.fake

import com.study.platform.global.redis.RedisMessagePublisher

class FakeRedisMessagePublisher : RedisMessagePublisher {

    private val publishedChannels: MutableList<String> = ArrayList()

    override fun publish(channel: String, message: String) {
        publishedChannels.add(channel)
    }

    fun wasPublishedTo(channel: String): Boolean = publishedChannels.contains(channel)

    fun getPublishedChannels(): List<String> = publishedChannels
}
