package com.study.platform.support.fake;

import com.study.platform.domain.notification.model.Notification;
import com.study.platform.domain.notification.model.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public Page<Notification> findAllByReceiverId(UUID receiverId, Pageable pageable) {
        var list = store.values().stream()
                .filter(n -> n.getReceiver().getId().equals(receiverId))
                .toList();
        return new PageImpl<>(list, pageable, list.size());
    }

    @Override
    public long countByReceiverIdAndIsReadFalse(UUID receiverId) {
        return store.values().stream()
                .filter(n -> n.getReceiver().getId().equals(receiverId) && !n.isRead())
                .count();
    }

    @Override
    public void markAllAsRead(UUID receiverId) {
        store.values().stream()
                .filter(n -> n.getReceiver().getId().equals(receiverId))
                .forEach(Notification::markAsRead);
    }
}
