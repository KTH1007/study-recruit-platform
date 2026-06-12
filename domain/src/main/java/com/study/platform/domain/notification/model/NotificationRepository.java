package com.study.platform.domain.notification.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);
    Optional<Notification> findById(UUID id);
    Page<Notification> findAllByReceiverId(UUID receiverId, Pageable pageable);
    long countByReceiverIdAndIsReadFalse(UUID receiverId);
    void markAllAsRead(UUID receiverId);
}
