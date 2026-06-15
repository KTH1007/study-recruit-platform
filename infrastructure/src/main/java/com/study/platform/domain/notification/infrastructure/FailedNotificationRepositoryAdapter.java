package com.study.platform.domain.notification.infrastructure;

import com.study.platform.domain.notification.model.FailedNotification;
import com.study.platform.domain.notification.model.FailedNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FailedNotificationRepositoryAdapter implements FailedNotificationRepository {

    private final FailedNotificationJpaRepository failedNotificationJpaRepository;

    @Override
    public FailedNotification save(FailedNotification failedNotification) {
        return failedNotificationJpaRepository.save(failedNotification);
    }
}
