package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.model.NotificationRepository;
import com.study.platform.domain.notification.usecase.MarkAllNotificationsAsReadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarkAllNotificationsAsReadService implements MarkAllNotificationsAsReadUseCase {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void execute(UUID userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
