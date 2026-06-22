package com.study.platform.support.fake

import com.study.platform.domain.notification.model.Notification
import com.study.platform.domain.notification.model.NotificationRepository
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.UUID

class FakeNotificationRepository : NotificationRepository {

    private val store: MutableMap<UUID, Notification> = HashMap()

    override fun save(notification: Notification): Notification {
        if (notification.id == null) {
            ReflectionTestUtils.setField(notification, "id", UUID.randomUUID())
        }
        if (notification.createdAt == null) {
            ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.now())
        }
        ReflectionTestUtils.setField(notification, "updatedAt", LocalDateTime.now())
        store[notification.id!!] = notification
        return notification
    }

    override fun findById(id: UUID): Notification? = store[id]

    override fun markAllAsRead(receiverId: UUID) {
        store.values
            .filter { n -> n.receiver?.id == receiverId }
            .forEach { it.markAsRead() }
    }

    fun countByReceiverIdAndIsReadFalse(receiverId: UUID): Long =
        store.values.count { n -> n.receiver?.id == receiverId && !n.isRead }.toLong()

    fun findAllByReceiverId(receiverId: UUID): List<Notification> =
        store.values.filter { n -> n.receiver?.id == receiverId }
}
