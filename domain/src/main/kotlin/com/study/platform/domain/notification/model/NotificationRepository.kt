package com.study.platform.domain.notification.model

import java.util.UUID

interface NotificationRepository {

    fun save(notification: Notification): Notification
    fun findById(id: UUID): Notification?
    fun markAllAsRead(receiverId: UUID)
}
