package com.study.platform.global.outbox.application

import com.study.platform.global.outbox.model.OutboxEvent
import com.study.platform.global.outbox.model.OutboxEventStatus
import com.study.platform.support.fake.FakeOutboxEventRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime

class OutboxEventServiceTest {

    private lateinit var outboxEventRepository: FakeOutboxEventRepository
    private lateinit var outboxEventService: OutboxEventService

    @BeforeEach
    fun setUp() {
        outboxEventRepository = FakeOutboxEventRepository()
        outboxEventService = OutboxEventService(outboxEventRepository)
    }

    @Test
    fun `save_정상_PENDING_저장`() {
        // given
        val topic = "post-sync"
        val aggregateId = "postId-123"
        val payload = "{\"postId\":\"123\"}"

        // when
        outboxEventService.save(topic, aggregateId, payload)

        // then
        val events = outboxEventRepository.findAllByStatusAndCreatedAtBefore(
            OutboxEventStatus.PENDING, LocalDateTime.MAX
        )
        assertThat(events).hasSize(1)
        assertThat(events[0].topic).isEqualTo("post-sync")
    }

    @Test
    fun `markSent_정상_SENT_업데이트`() {
        // given
        val event = OutboxEvent.pending("post-sync", "postId-123", "{}")
        ReflectionTestUtils.setField(event, "id", 1L)
        outboxEventRepository.save(event)

        // when
        outboxEventService.markSent(1L)

        // then
        val sentEvents = outboxEventRepository.findAllByStatusAndCreatedAtBefore(
            OutboxEventStatus.SENT, LocalDateTime.MAX
        )
        assertThat(sentEvents).hasSize(1)
    }
}
