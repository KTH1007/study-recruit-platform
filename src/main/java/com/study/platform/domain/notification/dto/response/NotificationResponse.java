package com.study.platform.domain.notification.dto.response;

import com.study.platform.domain.notification.model.Notification;
import com.study.platform.domain.notification.model.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID receiverId,
        NotificationType type,
        String message,
        UUID targetId,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getReceiver().getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getTargetId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
