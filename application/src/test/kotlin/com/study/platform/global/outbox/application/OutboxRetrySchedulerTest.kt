package com.study.platform.global.outbox.application

import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.event.PostSyncOperationType
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.outbox.model.OutboxEvent
import com.study.platform.support.fake.FakeFailedPostSyncRepository
import com.study.platform.support.fake.FakeKafkaMessagePublisher
import com.study.platform.support.fake.FakeOutboxEventRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class OutboxRetrySchedulerTest {

    @Mock
    private lateinit var outboxEventService: OutboxEventService

    private lateinit var outboxEventRepository: FakeOutboxEventRepository
    private lateinit var kafkaPublisher: FakeKafkaMessagePublisher
    private lateinit var failedPostSyncRepository: FakeFailedPostSyncRepository
    private lateinit var outboxRetryScheduler: OutboxRetryScheduler
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        outboxEventRepository = FakeOutboxEventRepository()
        kafkaPublisher = FakeKafkaMessagePublisher()
        failedPostSyncRepository = FakeFailedPostSyncRepository()
        outboxRetryScheduler = OutboxRetryScheduler(
            outboxEventRepository, outboxEventService, kafkaPublisher, failedPostSyncRepository, objectMapper
        )
    }

    private fun pendingOutbox(topic: String, retryCount: Int): OutboxEvent {
        val outbox = OutboxEvent.pending(topic, UUID.randomUUID().toString(), "{}")
        ReflectionTestUtils.setField(outbox, "id", 1L)
        ReflectionTestUtils.setField(outbox, "retryCount", retryCount)
        outboxEventRepository.save(outbox)
        return outbox
    }

    @Test
    fun `retryPendingEvents_Kafka_성공_markSent_호출`() {
        // given
        val outbox = pendingOutbox(KafkaConstants.POST_SYNC_TOPIC, 0)

        // when
        outboxRetryScheduler.retryPendingEvents()

        // then
        then(outboxEventService).should().markSent(outbox.id!!)
    }

    @Test
    fun `retryPendingEvents_Kafka_실패_재시도횟수_증가`() {
        // given
        val outbox = pendingOutbox(KafkaConstants.POST_SYNC_TOPIC, 0)
        kafkaPublisher.willFail()

        // when
        outboxRetryScheduler.retryPendingEvents()

        // then
        then(outboxEventService).should().incrementRetryCount(outbox.id!!)
        then(outboxEventService).should(never()).markSent(anyLong())
    }

    @Test
    fun `retryPendingEvents_최대재시도_초과_PostSync_FailedPostSync_저장`() {
        // given
        val syncEvent = PostSyncEvent(UUID.randomUUID(), PostSyncOperationType.UPSERT, 1L, 3)
        val payload = objectMapper.writeValueAsString(syncEvent)
        val outbox = OutboxEvent.pending(KafkaConstants.POST_SYNC_TOPIC, UUID.randomUUID().toString(), payload)
        ReflectionTestUtils.setField(outbox, "id", 1L)
        ReflectionTestUtils.setField(outbox, "retryCount", 3)
        outboxEventRepository.save(outbox)
        kafkaPublisher.willFail()

        // when
        outboxRetryScheduler.retryPendingEvents()

        // then
        assertThat(failedPostSyncRepository.getSaved()).hasSize(1)
        then(outboxEventService).should().markFailedPermanently(outbox.id!!)
    }

    @Test
    fun `retryPendingEvents_최대재시도_초과_Notification_FailedPostSync_미저장`() {
        // given
        pendingOutbox(KafkaConstants.NOTIFICATION_TOPIC, 3)
        kafkaPublisher.willFail()

        // when
        outboxRetryScheduler.retryPendingEvents()

        // then
        assertThat(failedPostSyncRepository.getSaved()).isEmpty()
        then(outboxEventService).should().markFailedPermanently(anyLong())
    }
}
