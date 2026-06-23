package com.study.platform.domain.post.application

import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.event.PostSyncOperationType
import com.study.platform.global.outbox.application.OutboxEventService
import com.study.platform.support.fake.FakeKafkaMessagePublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class PostSyncKafkaProducerTest {

    @Mock
    private lateinit var outboxEventService: OutboxEventService

    private lateinit var kafkaPublisher: FakeKafkaMessagePublisher
    private lateinit var postSyncKafkaProducer: PostSyncKafkaProducer
    private lateinit var event: PostSyncEvent

    @BeforeEach
    fun setUp() {
        kafkaPublisher = FakeKafkaMessagePublisher()
        postSyncKafkaProducer = PostSyncKafkaProducer(kafkaPublisher, ObjectMapper(), outboxEventService)
        event = PostSyncEvent(UUID.randomUUID(), PostSyncOperationType.UPSERT, 1L, 0)
    }

    @Test
    fun `handle_Kafka_발행_성공_markSent_호출`() {
        // given
        // event is set up in setUp, kafkaPublisher is configured to succeed

        // when
        postSyncKafkaProducer.handle(event)

        // then
        assertThat(kafkaPublisher.getPublishedTopics()).isNotEmpty()
        then(outboxEventService).should().markSent(eq(1L))
    }

    @Test
    fun `handle_Kafka_발행_실패_markSent_미호출`() {
        // given
        kafkaPublisher.willFail()

        // when
        postSyncKafkaProducer.handle(event)

        // then
        then(outboxEventService).should(never()).markSent(anyLong())
    }
}
