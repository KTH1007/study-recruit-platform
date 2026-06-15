package com.study.platform.domain.notification.model;

import java.util.UUID;

public interface NotificationPublisher {

    void send(Long outboxEventId, UUID receiverId, NotificationType type, String message, UUID targetId);
}
