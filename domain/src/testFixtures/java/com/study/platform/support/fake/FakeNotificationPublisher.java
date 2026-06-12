package com.study.platform.support.fake;

import com.study.platform.domain.notification.model.NotificationPublisher;
import com.study.platform.domain.notification.model.NotificationType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakeNotificationPublisher implements NotificationPublisher {

    public record SentNotification(UUID receiverId, NotificationType type, String message) {}

    private final List<SentNotification> sent = new ArrayList<>();

    @Override
    public void send(Long outboxEventId, UUID receiverId, NotificationType type, String message, UUID targetId) {
        sent.add(new SentNotification(receiverId, type, message));
    }

    public List<SentNotification> getSent() {
        return sent;
    }

    public boolean hasSentTo(UUID receiverId) {
        return sent.stream().anyMatch(n -> n.receiverId().equals(receiverId));
    }
}
