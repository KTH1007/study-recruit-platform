package com.study.platform.domain.notification.model

import java.util.UUID

interface NotificationPublisher {
    fun send(outboxEventId: Long, receiverId: UUID, type: NotificationType, message: String, targetId: UUID?)
}
