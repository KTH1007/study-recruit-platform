package com.study.platform.domain.post.application

import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.event.PostSyncOperationType
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.support.fake.FakeAcknowledgment
import com.study.platform.support.fake.FakeFailedPostSyncRepository
import com.study.platform.support.fake.FakeStudyPostRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.BDDMockito.then
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class PostSyncKafkaConsumerTest {

    @Mock
    private lateinit var postSearchService: PostSearchService

    private lateinit var ack: FakeAcknowledgment

    private lateinit var studyPostRepository: FakeStudyPostRepository
    private lateinit var failedPostSyncRepository: FakeFailedPostSyncRepository
    private lateinit var postSyncKafkaConsumer: PostSyncKafkaConsumer
    private lateinit var objectMapper: ObjectMapper

    private lateinit var postId: UUID

    @BeforeEach
    fun setUp() {
        ack = FakeAcknowledgment()
        objectMapper = ObjectMapper()
        studyPostRepository = FakeStudyPostRepository()
        failedPostSyncRepository = FakeFailedPostSyncRepository()
        postSyncKafkaConsumer = PostSyncKafkaConsumer(objectMapper, studyPostRepository, postSearchService, failedPostSyncRepository)
        postId = UUID.randomUUID()
    }

    @Test
    fun `consume_최대재시도초과_정상흐름_건너뛰고_DB저장`() {
        // given
        val event = PostSyncEvent(postId, PostSyncOperationType.UPSERT, 0L, KafkaConstants.MAX_DLT_RETRY)

        // when
        postSyncKafkaConsumer.consume(objectMapper.writeValueAsString(event), ack)

        // then
        assertThat(failedPostSyncRepository.getSaved()).hasSize(1)
        Mockito.verifyNoInteractions(postSearchService)
        assertThat(ack.isAcknowledged()).isTrue()
    }

    @Test
    fun `consume_UPSERT_정상처리`() {
        // given
        val event = PostSyncEvent(postId, PostSyncOperationType.UPSERT, 0L, 0)

        // when
        postSyncKafkaConsumer.consume(objectMapper.writeValueAsString(event), ack)

        // then
        assertThat(failedPostSyncRepository.getSaved()).isEmpty()
        Mockito.verifyNoInteractions(postSearchService)
        assertThat(ack.isAcknowledged()).isTrue()
    }

    @Test
    fun `consume_DELETE_정상처리`() {
        // given
        val event = PostSyncEvent(postId, PostSyncOperationType.DELETE, 0L, 0)

        // when
        postSyncKafkaConsumer.consume(objectMapper.writeValueAsString(event), ack)

        // then
        then(postSearchService).should().delete(postId.toString())
        assertThat(failedPostSyncRepository.getSaved()).isEmpty()
        assertThat(ack.isAcknowledged()).isTrue()
    }
}
