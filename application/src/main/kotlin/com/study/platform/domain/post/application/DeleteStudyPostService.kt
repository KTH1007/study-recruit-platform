package com.study.platform.domain.post.application

import com.study.platform.domain.apply.model.ApplyRepository
import com.study.platform.domain.comment.model.CommentRepository
import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.event.PostSyncOperationType
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.post.usecase.DeleteStudyPostUseCase
import com.study.platform.domain.team.model.StudyTeamRepository
import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.domain.team.model.TeamScheduleRepository
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
    private val studyTeamRepository: StudyTeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val teamScheduleRepository: TeamScheduleRepository,
    private val commentRepository: CommentRepository,
    private val applyRepository: ApplyRepository,
    private val eventPublisher: DomainEventPublisher,
    private val outboxEventService: OutboxEventService,
    private val objectMapper: ObjectMapper
) : DeleteStudyPostUseCase {

    @Transactional
    @CacheEvict(cacheNames = [CacheConstants.POST_CACHE], key = "#postId")
    override fun execute(userId: UUID, postId: UUID) {
        val post = studyPostRepository.findById(postId)
            ?: throw CustomException(ErrorCode.POST_NOT_FOUND)
        post.validateAuthor(userId)

        studyTeamRepository.findByPostId(postId)?.let { team ->
            val teamId = checkNotNull(team.id)
            teamScheduleRepository.deleteAllByTeamId(teamId)
            teamMemberRepository.deleteAllByTeamId(teamId)
            studyTeamRepository.delete(team)
        }
        commentRepository.deleteAllByPostId(postId)
        applyRepository.deleteAllByPostId(postId)
        studyPostRepository.delete(post)

        val outboxEventId = saveOutboxEvent(postId, PostSyncOperationType.DELETE)
        eventPublisher.publish(PostSyncEvent(postId, PostSyncOperationType.DELETE, outboxEventId, 0))
    }

    private fun saveOutboxEvent(postId: UUID, operationType: PostSyncOperationType): Long =
        outboxEventService.saveWithIdEmbeddedInTx(KafkaConstants.POST_SYNC_TOPIC, postId.toString()) { id ->
            objectMapper.writeValueAsString(PostSyncEvent(postId, operationType, id, 0))
        }
}
