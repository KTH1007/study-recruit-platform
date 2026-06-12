package com.study.platform.domain.notification.infrastructure;

import com.study.platform.domain.notification.model.Notification;
import com.study.platform.domain.notification.model.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public Notification save(Notification notification) {
        return notificationJpaRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return notificationJpaRepository.findById(id);
    }

    @Override
    public Page<Notification> findAllByReceiverId(UUID receiverId, Pageable pageable) {
        return notificationJpaRepository.findAllByReceiverId(receiverId, pageable);
    }

    @Override
    public long countByReceiverIdAndIsReadFalse(UUID receiverId) {
        return notificationJpaRepository.countByReceiverIdAndIsReadFalse(receiverId);
    }

    @Override
    public void markAllAsRead(UUID receiverId) {
        notificationJpaRepository.markAllAsRead(receiverId);
    }
}
