package com.study.platform.support.fake

import com.study.platform.global.outbox.model.OutboxEvent
import com.study.platform.global.outbox.model.OutboxEventRepository
import com.study.platform.global.outbox.model.OutboxEventStatus
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
        store.add(event)
        return event
    }

    override fun findAllByStatusAndCreatedAtBefore(status: OutboxEventStatus, createdAt: LocalDateTime): List<OutboxEvent> =
        store.filter { e -> e.status == status }

    override fun markSentById(id: Long): Int =
        store.filter { e -> e.id == id }
            .sumOf { e ->
                ReflectionTestUtils.setField(e, "status", OutboxEventStatus.SENT)
                1
            }

    override fun markFailedPermanently(id: Long) {
        store.filter { e -> e.id == id }
            .forEach { e -> ReflectionTestUtils.setField(e, "status", OutboxEventStatus.FAILED_PERMANENTLY) }
    }
}
