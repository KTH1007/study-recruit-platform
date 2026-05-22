package com.study.platform.domain.notification.dto.event;

import com.study.platform.domain.notification.model.NotificationType;

import java.util.UUID;

public record NotificationEvent(
        UUID receiverId,
        NotificationType type,
        String message,
        UUID targetId,
        int retryCount
) {
    public NotificationEvent withRetry() {
        return new NotificationEvent(receiverId, type, message, targetId, retryCount + 1);
    }
}
