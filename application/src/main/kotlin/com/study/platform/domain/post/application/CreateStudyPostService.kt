package com.study.platform.domain.post.application

import com.study.platform.domain.post.dto.request.StudyPostCreateRequest
import com.study.platform.domain.post.dto.response.StudyPostResponse
import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.event.PostSyncOperationType
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.post.usecase.CreateStudyPostUseCase
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.event.DomainEventPublisher
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.outbox.application.OutboxEventService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Service
class CreateStudyPostService(
    private val studyPostRepository: StudyPostRepository,
    private val userRepository: UserRepository,
    private val eventPublisher: DomainEventPublisher,
    private val outboxEventService: OutboxEventService,
    private val objectMapper: ObjectMapper
) : CreateStudyPostUseCase {

    @Transactional
    override fun execute(userId: UUID, request: StudyPostCreateRequest): StudyPostResponse {
        val user = userRepository.findById(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        val post = studyPostRepository.save(
            StudyPost.create(user, request.title, request.description, request.techStack, request.maxMembers, request.deadline)
        )
        val outboxEventId = saveOutboxEvent(post.id!!, PostSyncOperationType.UPSERT)
        eventPublisher.publish(PostSyncEvent(post.id!!, PostSyncOperationType.UPSERT, outboxEventId, 0))
        return StudyPostResponse.from(post)
    }

    private fun saveOutboxEvent(postId: UUID, operationType: PostSyncOperationType): Long {
        val event = PostSyncEvent(postId, operationType, 0L, 0)
        val payload = objectMapper.writeValueAsString(event)
        return outboxEventService.save(KafkaConstants.POST_SYNC_TOPIC, postId.toString(), payload)
    }
}
