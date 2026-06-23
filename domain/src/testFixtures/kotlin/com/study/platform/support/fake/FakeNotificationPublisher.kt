package com.study.platform.support.fake

import com.study.platform.domain.notification.model.NotificationPublisher
import com.study.platform.domain.notification.model.NotificationType
import java.util.UUID

class FakeNotificationPublisher : NotificationPublisher {

    data class SentNotification(val receiverId: UUID, val type: NotificationType, val message: String)

    private val sent: MutableList<SentNotification> = ArrayList()

    override fun send(outboxEventId: Long, receiverId: UUID, type: NotificationType, message: String, targetId: UUID?) {
        sent.add(SentNotification(receiverId, type, message))
    }

    fun getSent(): List<SentNotification> = sent

    fun hasSentTo(receiverId: UUID): Boolean =
        sent.any { n -> n.receiverId == receiverId }
}
