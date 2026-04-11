package com.study.platform.domain.notification.dto.event;

import com.study.platform.domain.notification.model.NotificationType;

import java.util.UUID;

public record NotificationEvent(
        UUID receiverId,
        NotificationType type,
        String message,
        UUID targetId
) {
}
