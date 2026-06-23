package com.study.platform.domain.notification.infrastructure

import com.study.platform.domain.notification.model.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface NotificationJpaRepository : JpaRepository<Notification, UUID> {

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    fun markAllAsRead(@Param("receiverId") receiverId: UUID)
}
