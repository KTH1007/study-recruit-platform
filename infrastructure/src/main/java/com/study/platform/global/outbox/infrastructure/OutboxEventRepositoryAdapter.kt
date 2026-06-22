package com.study.platform.global.outbox.infrastructure

import com.study.platform.global.outbox.model.OutboxEvent
import com.study.platform.global.outbox.model.OutboxEventRepository
import com.study.platform.global.outbox.model.OutboxEventStatus
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class OutboxEventRepositoryAdapter(
    private val outboxEventJpaRepository: OutboxEventJpaRepository
) : OutboxEventRepository {

    override fun save(event: OutboxEvent): OutboxEvent =
        outboxEventJpaRepository.save(event)

    override fun findAllByStatusAndCreatedAtBefore(status: OutboxEventStatus, createdAt: LocalDateTime): List<OutboxEvent> =
        outboxEventJpaRepository.findAllByStatusAndCreatedAtBefore(status, createdAt)

    override fun markSentById(id: Long): Int =
        outboxEventJpaRepository.markSentById(id)

    override fun markFailedPermanently(id: Long) {
        outboxEventJpaRepository.markFailedPermanently(id)
    }
}
