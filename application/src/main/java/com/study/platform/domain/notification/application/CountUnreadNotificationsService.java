package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.port.NotificationQueryPort;
import com.study.platform.domain.notification.usecase.CountUnreadNotificationsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CountUnreadNotificationsService implements CountUnreadNotificationsUseCase {

    private final NotificationQueryPort notificationQueryPort;

    @Override
    public long execute(UUID userId) {
        return notificationQueryPort.countByReceiverIdAndIsReadFalse(userId);
    }
}
