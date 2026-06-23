package com.study.platform.global.outbox.model

import com.study.platform.global.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "outbox_event")
class OutboxEvent : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var topic: String = ""

    @Column(nullable = false)
    var messageKey: String = ""

    @Column(nullable = false, columnDefinition = "TEXT")
    var payload: String = ""

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OutboxEventStatus = OutboxEventStatus.PENDING

    @Column(nullable = false)
    var retryCount: Int = 0

    var sentAt: LocalDateTime? = null

    fun incrementRetryCount() {
        retryCount++
    }

    companion object {
        fun pending(topic: String, messageKey: String, payload: String): OutboxEvent =
            OutboxEvent().also {
                it.topic = topic
                it.messageKey = messageKey
                it.payload = payload
                it.status = OutboxEventStatus.PENDING
            }
    }
}
