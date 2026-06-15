package com.study.platform.domain.notification.usecase;

import java.util.UUID;

public interface CountUnreadNotificationsUseCase {
    long execute(UUID userId);
}
