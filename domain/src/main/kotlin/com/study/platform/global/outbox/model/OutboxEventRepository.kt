package com.study.platform.global.outbox.model

import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface OutboxEventRepository {

    fun save(event: OutboxEvent): OutboxEvent
    fun findById(id: Long): OutboxEvent?
    fun findAllByStatusAndCreatedAtBefore(status: OutboxEventStatus, createdAt: LocalDateTime, pageable: Pageable): List<OutboxEvent>
    fun markSentById(id: Long): Int
    fun markFailedPermanently(id: Long)
}
