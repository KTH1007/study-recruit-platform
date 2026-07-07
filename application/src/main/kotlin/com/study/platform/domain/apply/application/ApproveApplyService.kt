package com.study.platform.domain.apply.application

import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.domain.apply.model.ApplyRepository
import com.study.platform.domain.apply.model.ApplyStatus
import com.study.platform.domain.apply.usecase.ApproveApplyUseCase
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.global.event.DomainEventPublisher
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ApproveApplyService(
    private val applyRepository: ApplyRepository,
    private val studyPostRepository: StudyPostRepository,
    private val eventPublisher: DomainEventPublisher
) : ApproveApplyUseCase {

    @Transactional
    override fun execute(userId: UUID, applyId: UUID): ApplyResponse {
        // post -> apply 순서로 락을 획득하여 REPEATABLE_READ 스냅샷 문제 방지
        val postId = applyRepository.findPostIdByApplyId(applyId)
            ?: throw CustomException(ErrorCode.APPLICATION_NOT_FOUND)

        val post = studyPostRepository.findByIdWithAuthorForUpdate(postId)
            ?: throw CustomException(ErrorCode.POST_NOT_FOUND)
        post.validateAuthor(userId)
        post.validateOpen()

        val apply = applyRepository.findByIdWithPostAndApplicantForUpdate(applyId)
            ?: throw CustomException(ErrorCode.APPLICATION_NOT_FOUND)

        apply.approve(eventPublisher)
        applyRepository.save(apply)

        val approvedCount = applyRepository.countByPostIdAndStatusForUpdate(postId, ApplyStatus.APPROVED)
        post.markFullIfNeeded(approvedCount)

        return ApplyResponse.from(apply)
    }
}
