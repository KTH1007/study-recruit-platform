package com.study.platform.domain.notification.model

import com.study.platform.domain.user.model.User
import com.study.platform.global.entity.BaseTimeEntity
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "notifications",
    indexes = [
        Index(name = "idx_notification_receiver_read_created", columnList = "receiver_id, is_read, created_at"),
        Index(name = "idx_notification_receiver_created", columnList = "receiver_id, created_at")
    ]
)
class Notification : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    var receiver: User? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var type: NotificationType? = null

    @Column(nullable = false)
    var message: String = ""

    @Column(columnDefinition = "BINARY(16)")
    var targetId: UUID? = null

    @Column(nullable = false)
    var isRead: Boolean = false

    fun markAsRead() {
        isRead = true
    }

    fun validateReceiver(userId: UUID) {
        if (receiver?.id != userId) throw CustomException(ErrorCode.FORBIDDEN)
    }

    companion object {
        fun create(receiver: User, type: NotificationType, message: String, targetId: UUID?): Notification =
            Notification().also {
                it.receiver = receiver
                it.type = type
                it.message = message
                it.targetId = targetId
            }
    }
}
