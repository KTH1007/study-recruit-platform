package com.study.platform.domain.notification.model;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);
    Optional<Notification> findById(UUID id);
    void markAllAsRead(UUID receiverId);
}
