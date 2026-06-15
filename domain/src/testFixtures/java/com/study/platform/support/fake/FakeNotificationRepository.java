package com.study.platform.support.fake;

import com.study.platform.domain.notification.model.Notification;
import com.study.platform.domain.notification.model.NotificationRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeNotificationRepository implements NotificationRepository {

    private final Map<UUID, Notification> store = new HashMap<>();

    @Override
    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());
        }
        store.put(notification.getId(), notification);
        return notification;
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void markAllAsRead(UUID receiverId) {
        store.values().stream()
                .filter(n -> n.getReceiver().getId().equals(receiverId))
                .forEach(Notification::markAsRead);
    }

    public long countByReceiverIdAndIsReadFalse(UUID receiverId) {
        return store.values().stream()
                .filter(n -> n.getReceiver().getId().equals(receiverId) && !n.isRead())
                .count();
    }

    public java.util.List<Notification> findAllByReceiverId(UUID receiverId) {
        return store.values().stream()
                .filter(n -> n.getReceiver().getId().equals(receiverId))
                .toList();
    }
}
