package com.study.platform.support.fake;

import com.study.platform.domain.notification.dto.response.NotificationResponse;
import com.study.platform.domain.notification.port.NotificationQueryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public class FakeNotificationQueryPort implements NotificationQueryPort {

    private final FakeNotificationRepository repository;

    public FakeNotificationQueryPort(FakeNotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<NotificationResponse> findAllByReceiverId(UUID receiverId, Pageable pageable) {
        throw new UnsupportedOperationException("필요 시 구현");
    }

    @Override
    public long countByReceiverIdAndIsReadFalse(UUID receiverId) {
        return repository.countByReceiverIdAndIsReadFalse(receiverId);
    }
}
