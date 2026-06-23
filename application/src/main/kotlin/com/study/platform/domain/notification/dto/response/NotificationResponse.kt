package com.study.platform.domain.notification.dto.response

import com.study.platform.domain.notification.model.Notification
import com.study.platform.domain.notification.model.NotificationType
import java.time.LocalDateTime
import java.util.UUID

data class NotificationResponse(
    val id: UUID,
    val receiverId: UUID,
    val type: NotificationType,
    val message: String,
    val targetId: UUID?,
    val isRead: Boolean,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(notification: Notification): NotificationResponse = NotificationResponse(
            id = notification.id!!,
            receiverId = notification.receiver!!.id!!,
            type = notification.type!!,
            message = notification.message,
            targetId = notification.targetId,
            isRead = notification.isRead,
            createdAt = notification.createdAt!!
        )
    }
}
