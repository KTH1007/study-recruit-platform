package com.study.platform.domain.notification.infrastructure;

import com.study.platform.domain.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    void markAllAsRead(@Param("receiverId") UUID receiverId);
}
