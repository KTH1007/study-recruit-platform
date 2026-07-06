package com.study.platform.support.fake

import com.study.platform.global.outbox.model.OutboxEvent
import com.study.platform.global.outbox.model.OutboxEventRepository
import com.study.platform.global.outbox.model.OutboxEventStatus
import org.springframework.data.domain.Pageable
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

class FakeOutboxEventRepository : OutboxEventRepository {

    private val store: MutableList<OutboxEvent> = ArrayList()
    private val sequence = AtomicLong(1)

    override fun save(event: OutboxEvent): OutboxEvent {
        if (event.id == null) {
            ReflectionTestUtils.setField(event, "id", sequence.getAndIncrement())
        }
        if (event.createdAt == null) {
            event.createdAt = LocalDateTime.now()
        }
        store.add(event)
        return event
    }

    override fun findAllByStatusAndCreatedAtBefore(status: OutboxEventStatus, createdAt: LocalDateTime, pageable: Pageable): List<OutboxEvent> =
        store.filter { e -> e.status == status && e.createdAt?.isBefore(createdAt) == true }
            .drop(pageable.offset.toInt())
            .take(pageable.pageSize)

    override fun markSentById(id: Long): Int =
        store.filter { e -> e.id == id }
            .sumOf { e ->
                ReflectionTestUtils.setField(e, "status", OutboxEventStatus.SENT)
                1
            }

    override fun findById(id: Long): OutboxEvent? =
        store.find { it.id == id }

    override fun markFailedPermanently(id: Long) {
        store.filter { e -> e.id == id }
            .forEach { e -> ReflectionTestUtils.setField(e, "status", OutboxEventStatus.FAILED_PERMANENTLY) }
    }

}
