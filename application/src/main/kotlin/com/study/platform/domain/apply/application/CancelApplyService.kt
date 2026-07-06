package com.study.platform.domain.apply.application

import com.study.platform.domain.apply.model.ApplyRepository
import com.study.platform.domain.apply.usecase.CancelApplyUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CancelApplyService(
    private val applyRepository: ApplyRepository
) : CancelApplyUseCase {

    @Transactional
    override fun execute(userId: UUID, postId: UUID) {
        val apply = applyRepository.findByPostIdAndApplicantId(postId, userId)
            ?: throw CustomException(ErrorCode.APPLICATION_NOT_FOUND)
        apply.validatePending()
        applyRepository.delete(apply)
    }
}