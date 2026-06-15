package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.dto.response.NotificationResponse;
import com.study.platform.domain.notification.port.NotificationQueryPort;
import com.study.platform.domain.notification.usecase.FindNotificationsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindNotificationsService implements FindNotificationsUseCase {

    private final NotificationQueryPort notificationQueryPort;

    @Override
    public Page<NotificationResponse> execute(UUID userId, Pageable pageable) {
        return notificationQueryPort.findAllByReceiverId(userId, pageable);
    }
}
