package com.study.platform.domain.notification.usecase;

import java.util.UUID;

public interface MarkAllNotificationsAsReadUseCase {
    void execute(UUID userId);
}
