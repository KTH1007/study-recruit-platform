package com.study.platform.domain.post.application

import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.event.PostSyncOperationType
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.post.usecase.DeleteStudyPostUseCase
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
class DeleteStudyPostService(
    private val studyPostRepository: StudyPostRepository,
    private val eventPublisher: DomainEventPublisher,
    private val outboxEventService: OutboxEventService,
    private val objectMapper: ObjectMapper
) : DeleteStudyPostUseCase {

    @Transactional
    @CacheEvict(cacheNames = [CacheConstants.POST_CACHE], key = "#postId")
    override fun execute(userId: UUID, postId: UUID) {
        val post = studyPostRepository.findByIdWithAuthor(postId)
            
            ?: throw CustomException(ErrorCode.POST_NOT_FOUND)
        post.validateAuthor(userId)
        studyPostRepository.delete(post)
        val outboxEventId = saveOutboxEvent(postId, PostSyncOperationType.DELETE)
        eventPublisher.publish(PostSyncEvent(postId, PostSyncOperationType.DELETE, outboxEventId, 0))
    }

    private fun saveOutboxEvent(postId: UUID, operationType: PostSyncOperationType): Long {
        val event = PostSyncEvent(postId, operationType, 0L, 0)
        val payload = objectMapper.writeValueAsString(event)
        return outboxEventService.save(KafkaConstants.POST_SYNC_TOPIC, postId.toString(), payload)
    }
}
