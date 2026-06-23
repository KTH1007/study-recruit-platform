package com.study.platform.domain.notification.model

import com.study.platform.global.entity.BaseTimeEntity
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "failed_notifications")
class FailedNotification : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    var id: UUID? = null

    @Column(nullable = false, columnDefinition = "BINARY(16)")
    var receiverId: UUID? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var type: NotificationType? = null

    @Column(nullable = false)
    var message: String = ""

    @Column(columnDefinition = "BINARY(16)")
    var targetId: UUID? = null

    @Column(columnDefinition = "text")
    var failureReason: String? = null

    companion object {
        fun from(event: NotificationEvent, failureReason: String): FailedNotification =
            FailedNotification().also {
                it.receiverId = event.receiverId
                it.type = event.type
                it.message = event.message
                it.targetId = event.targetId
                it.failureReason = failureReason
            }
    }
}
