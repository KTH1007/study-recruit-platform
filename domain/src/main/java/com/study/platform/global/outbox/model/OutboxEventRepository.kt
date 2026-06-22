package com.study.platform.global.outbox.model

import java.time.LocalDateTime

interface OutboxEventRepository {

    fun save(event: OutboxEvent): OutboxEvent
    fun findAllByStatusAndCreatedAtBefore(status: OutboxEventStatus, createdAt: LocalDateTime): List<OutboxEvent>
    fun markSentById(id: Long): Int
    fun markFailedPermanently(id: Long)
}
