package com.study.platform.domain.notification.usecase;

import com.study.platform.domain.notification.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FindNotificationsUseCase {
    Page<NotificationResponse> execute(UUID userId, Pageable pageable);
}
