package com.study.platform.domain.apply.application

import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.domain.apply.port.ApplyQueryPort
import com.study.platform.domain.apply.usecase.FindAppliesUseCase
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FindAppliesService(
    private val applyQueryPort: ApplyQueryPort,
    private val studyPostRepository: StudyPostRepository
) : FindAppliesUseCase {

    override fun execute(userId: UUID, postId: UUID, pageable: Pageable): Page<ApplyResponse> {
        val post = studyPostRepository.findById(postId)
            ?: throw CustomException(ErrorCode.POST_NOT_FOUND)
        post.validateAuthor(userId)
        return applyQueryPort.findAllByPostId(postId, pageable)
    }
}