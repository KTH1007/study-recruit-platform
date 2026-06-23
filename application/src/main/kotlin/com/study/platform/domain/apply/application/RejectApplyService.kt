package com.study.platform.domain.apply.application

import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.domain.apply.model.ApplyRepository
import com.study.platform.domain.apply.usecase.RejectApplyUseCase
import com.study.platform.global.event.DomainEventPublisher
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RejectApplyService(
    private val applyRepository: ApplyRepository,
    private val eventPublisher: DomainEventPublisher
) : RejectApplyUseCase {

    @Transactional
    override fun execute(userId: UUID, applyId: UUID): ApplyResponse {
        val apply = applyRepository.findByIdWithPostAndApplicantForUpdate(applyId)
            ?: throw CustomException(ErrorCode.APPLICATION_NOT_FOUND)
        apply.post.validateAuthor(userId)
        apply.reject(eventPublisher)
        applyRepository.save(apply)
        return ApplyResponse.from(apply)
    }
}