package com.study.platform.domain.post.application

import com.study.platform.domain.post.dto.response.StudyPostResponse
import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.event.PostSyncOperationType
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.post.usecase.CloseStudyPostUseCase
import com.study.platform.global.constant.CacheConstants
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.event.DomainEventPublisher
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.outbox.application.OutboxEventService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Service
class CloseStudyPostService(
    private val studyPostRepository: StudyPostRepository,
    private val eventPublisher: DomainEventPublisher,
    private val outboxEventService: OutboxEventService,
    private val objectMapper: ObjectMapper
) : CloseStudyPostUseCase {

    @Transactional
    @CacheEvict(cacheNames = [CacheConstants.POST_CACHE], key = "#postId")
    override fun execute(userId: UUID, postId: UUID): StudyPostResponse {
        val post = studyPostRepository.findByIdWithAuthorForUpdate(postId)
            ?: throw CustomException(ErrorCode.POST_NOT_FOUND)
        post.validateAuthor(userId)
        post.close()
        val outboxEventId = saveOutboxEvent(postId, PostSyncOperationType.UPSERT)
        eventPublisher.publish(PostSyncEvent(postId, PostSyncOperationType.UPSERT, outboxEventId, 0))
        return StudyPostResponse.from(post)
    }

    private fun saveOutboxEvent(postId: UUID, operationType: PostSyncOperationType): Long =
        outboxEventService.saveWithIdEmbeddedInTx(KafkaConstants.POST_SYNC_TOPIC, postId.toString()) { id ->
            objectMapper.writeValueAsString(PostSyncEvent(postId, operationType, id, 0))
        }
}
