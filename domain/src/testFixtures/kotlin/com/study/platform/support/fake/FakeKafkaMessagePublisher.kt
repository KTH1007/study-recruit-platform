package com.study.platform.support.fake

import com.study.platform.global.kafka.KafkaMessagePublisher
import java.util.concurrent.CompletableFuture

class FakeKafkaMessagePublisher : KafkaMessagePublisher {

    data class PublishedMessage(val topic: String, val key: String?, val payload: String)

    private val published: MutableList<PublishedMessage> = ArrayList()
    private var shouldFail = false

    override fun publish(topic: String, key: String?, payload: String): CompletableFuture<Void> {
        if (shouldFail) {
            return CompletableFuture.failedFuture(RuntimeException("Kafka 연결 실패"))
        }
        published.add(PublishedMessage(topic, key, payload))
        return CompletableFuture.completedFuture(null)
    }

    fun willFail() {
        shouldFail = true
    }

    fun wasPublishedTo(topic: String): Boolean =
        published.any { m -> m.topic == topic }

    fun getPublishedTopics(): List<String> =
        published.map { it.topic }

    fun getPublished(): List<PublishedMessage> = published
}
