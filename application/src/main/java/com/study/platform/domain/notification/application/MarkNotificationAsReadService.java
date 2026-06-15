package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.model.Notification;
import com.study.platform.domain.notification.model.NotificationRepository;
import com.study.platform.domain.notification.usecase.MarkNotificationAsReadUseCase;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarkNotificationAsReadService implements MarkNotificationAsReadUseCase {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void execute(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.validateReceiver(userId);
        notification.markAsRead();
    }
}
