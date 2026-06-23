package com.study.platform.domain.post.application

import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.event.PostSyncOperationType
import com.study.platform.domain.post.model.FailedPostSync
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.support.fake.FakeFailedPostSyncRepository
import com.study.platform.support.fake.FakeKafkaMessagePublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.support.Acknowledgment
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class PostSyncDltConsumerTest {

    @Mock
    private lateinit var ack: Acknowledgment

    private lateinit var kafkaPublisher: FakeKafkaMessagePublisher
    private lateinit var failedPostSyncRepository: FakeFailedPostSyncRepository
    private lateinit var postSyncDltConsumer: PostSyncDltConsumer
    private lateinit var objectMapper: ObjectMapper

    private lateinit var postId: UUID

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        kafkaPublisher = FakeKafkaMessagePublisher()
        failedPostSyncRepository = FakeFailedPostSyncRepository()
        postSyncDltConsumer = PostSyncDltConsumer(objectMapper, kafkaPublisher, failedPostSyncRepository)
        postId = UUID.randomUUID()
    }

    @Test
    fun `consume_재시도횟수_미만_메인토픽_재투입`() {
        // given
        val event = PostSyncEvent(postId, PostSyncOperationType.UPSERT, 0L, 1)

        // when
        postSyncDltConsumer.consume(objectMapper.writeValueAsString(event), ack, "ES 연결 실패")

        // then
        assertThat(kafkaPublisher.wasPublishedTo(KafkaConstants.POST_SYNC_TOPIC)).isTrue()
        assertThat(failedPostSyncRepository.getSaved()).isEmpty()
        then(ack).should().acknowledge()
    }

    @Test
    fun `consume_재시도횟수_초과_DB_영구저장`() {
        // given
        val event = PostSyncEvent(postId, PostSyncOperationType.UPSERT, 0L, KafkaConstants.MAX_DLT_RETRY)

        // when
        postSyncDltConsumer.consume(objectMapper.writeValueAsString(event), ack, "ES 연결 실패")

        // then
        assertThat(failedPostSyncRepository.getSaved()).hasSize(1)
        assertThat(failedPostSyncRepository.getSaved()[0]).isInstanceOf(FailedPostSync::class.java)
        assertThat(kafkaPublisher.getPublishedTopics()).isEmpty()
        then(ack).should().acknowledge()
    }

    @Test
    fun `consume_페이로드_파싱_실패_ack_처리`() {
        // given
        val invalidPayload = "invalid-json"

        // when
        postSyncDltConsumer.consume(invalidPayload, ack, null)

        // then
        assertThat(kafkaPublisher.getPublishedTopics()).isEmpty()
        assertThat(failedPostSyncRepository.getSaved()).isEmpty()
        then(ack).should().acknowledge()
    }
}
