package com.study.platform.domain.notification.infrastructure

import com.study.platform.domain.notification.model.Notification
import com.study.platform.domain.notification.model.NotificationRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class NotificationRepositoryAdapter(
    private val notificationJpaRepository: NotificationJpaRepository
) : NotificationRepository {

    override fun save(notification: Notification): Notification =
        notificationJpaRepository.save(notification)

    override fun findById(id: UUID): Notification? =
        notificationJpaRepository.findById(id).orElse(null)

    override fun markAllAsRead(receiverId: UUID) {
        notificationJpaRepository.markAllAsRead(receiverId)
    }
}
