package com.study.platform.global.kafka;

import java.util.concurrent.CompletableFuture;

public interface KafkaMessagePublisher {

    CompletableFuture<Void> publish(String topic, String key, String payload);

    default CompletableFuture<Void> publish(String topic, String payload) {
        return publish(topic, null, payload);
    }
}
