package com.study.platform.domain.post.application

import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.event.PostSyncOperationType
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.outbox.application.OutboxEventService
import com.study.platform.global.outbox.model.OutboxEvent
import com.study.platform.global.outbox.model.OutboxEventStatus
import com.study.platform.support.fake.FakeKafkaMessagePublisher
import com.study.platform.support.fake.FakeOutboxEventRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import tools.jackson.databind.ObjectMapper

class PostSyncKafkaProducerTest {

    private lateinit var outboxEventRepository: FakeOutboxEventRepository
    private lateinit var outboxEventService: OutboxEventService
    private lateinit var kafkaPublisher: FakeKafkaMessagePublisher
    private lateinit var postSyncKafkaProducer: PostSyncKafkaProducer
    private lateinit var event: PostSyncEvent
    private lateinit var outbox: OutboxEvent

    @BeforeEach
    fun setUp() {
        outboxEventRepository = FakeOutboxEventRepository()
        kafkaPublisher = FakeKafkaMessagePublisher()
        outboxEventService = OutboxEventService(outboxEventRepository)
        postSyncKafkaProducer = PostSyncKafkaProducer(kafkaPublisher, ObjectMapper(), outboxEventService)

        outbox = OutboxEvent.pending(KafkaConstants.POST_SYNC_TOPIC, "key", "{}")
        ReflectionTestUtils.setField(outbox, "id", 1L)
        outboxEventRepository.save(outbox)

        event = PostSyncEvent(java.util.UUID.randomUUID(), PostSyncOperationType.UPSERT, 1L, 0)
    }

    @Test
    fun `handle_Kafka_발행_성공_SENT_상태로_변경`() {
        // given
        // event, outbox는 setUp에서 준비됨

        // when
        postSyncKafkaProducer.handle(event)

        // then
        assertThat(kafkaPublisher.getPublishedTopics()).isNotEmpty()
        assertThat(outbox.status).isEqualTo(OutboxEventStatus.SENT)
    }

    @Test
    fun `handle_Kafka_발행_실패_PENDING_상태_유지`() {
        // given
        kafkaPublisher.willFail()

        // when
        postSyncKafkaProducer.handle(event)

        // then
        assertThat(outbox.status).isEqualTo(OutboxEventStatus.PENDING)
    }
}
