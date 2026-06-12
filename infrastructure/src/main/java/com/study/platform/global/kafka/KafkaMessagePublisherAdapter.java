package com.study.platform.global.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class KafkaMessagePublisherAdapter implements KafkaMessagePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public CompletableFuture<Void> publish(String topic, String key, String payload) {
        return kafkaTemplate.send(topic, key, payload).thenApply(result -> null);
    }
}
