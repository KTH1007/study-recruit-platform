package com.study.platform.domain.notification.usecase;

import java.util.UUID;

public interface MarkNotificationAsReadUseCase {
    void execute(UUID userId, UUID notificationId);
}
