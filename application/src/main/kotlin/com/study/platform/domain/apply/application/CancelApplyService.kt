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
        val applyId = applyRepository.findByPostIdAndApplicantId(postId, userId)?.id
            ?: throw CustomException(ErrorCode.APPLICATION_NOT_FOUND)

        // Post 락이 필요 없으므로 Apply 행만 잠가 Approve(Post -> Apply)와의 역순 락으로 인한 데드락을 피한다.
        val apply = applyRepository.findByIdForUpdate(applyId)
            ?: throw CustomException(ErrorCode.APPLICATION_NOT_FOUND)
        apply.validatePending()
        applyRepository.delete(apply)
    }
}