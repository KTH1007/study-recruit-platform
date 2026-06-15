package com.study.platform.support.fake;

import com.study.platform.global.kafka.KafkaMessagePublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FakeKafkaMessagePublisher implements KafkaMessagePublisher {

    public record PublishedMessage(String topic, String key, String payload) {}

    private final List<PublishedMessage> published = new ArrayList<>();
    private boolean shouldFail = false;

    @Override
    public CompletableFuture<Void> publish(String topic, String key, String payload) {
        if (shouldFail) {
            return CompletableFuture.failedFuture(new RuntimeException("Kafka 연결 실패"));
        }
        published.add(new PublishedMessage(topic, key, payload));
        return CompletableFuture.completedFuture(null);
    }

    public void willFail() {
        shouldFail = true;
    }

    public boolean wasPublishedTo(String topic) {
        return published.stream().anyMatch(m -> m.topic().equals(topic));
    }

    public List<String> getPublishedTopics() {
        return published.stream().map(PublishedMessage::topic).toList();
    }

    public List<PublishedMessage> getPublished() {
        return published;
    }
}
