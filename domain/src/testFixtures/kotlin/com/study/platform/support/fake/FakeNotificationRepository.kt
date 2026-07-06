package com.study.platform.support.fake

import com.study.platform.domain.notification.model.Notification
import com.study.platform.domain.notification.model.NotificationRepository
import java.util.UUID

class FakeNotificationRepository : AbstractFakeUuidRepository<Notification>(), NotificationRepository {

    override fun idOf(entity: Notification): UUID? = entity.id
    override fun hasTimestamps() = true

    override fun save(notification: Notification): Notification = saveEntity(notification)

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
