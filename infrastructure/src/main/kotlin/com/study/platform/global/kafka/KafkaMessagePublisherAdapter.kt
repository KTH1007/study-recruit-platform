package com.study.platform.global.kafka

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class KafkaMessagePublisherAdapter(
    private val kafkaTemplate: KafkaTemplate<String, String>
) : KafkaMessagePublisher {

    override fun publish(topic: String, key: String?, payload: String): CompletableFuture<Void> =
        if (key != null) kafkaTemplate.send(topic, key, payload).thenApply { null }
        else kafkaTemplate.send(topic, payload).thenApply { null }
}
