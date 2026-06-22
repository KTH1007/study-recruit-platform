package com.study.platform.global.kafka

import java.util.concurrent.CompletableFuture

interface KafkaMessagePublisher {
    fun publish(topic: String, key: String?, payload: String): CompletableFuture<Void>

    fun publish(topic: String, payload: String): CompletableFuture<Void> =
        publish(topic, null, payload)
}
