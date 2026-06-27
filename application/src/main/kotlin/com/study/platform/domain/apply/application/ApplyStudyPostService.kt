package com.study.platform.domain.apply.application

import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.domain.apply.model.Apply
import com.study.platform.domain.apply.model.ApplyRepository
import com.study.platform.domain.apply.usecase.ApplyStudyPostUseCase
import com.study.platform.domain.post.model.StudyPostRepository
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.event.DomainEventPublisher
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ApplyStudyPostService(
    private val applyRepository: ApplyRepository,
    private val studyPostRepository: StudyPostRepository,
    private val userRepository: UserRepository,
    private val eventPublisher: DomainEventPublisher
) : ApplyStudyPostUseCase {

    @Transactional
    override fun execute(userId: UUID, postId: UUID, request: ApplyCreateRequest): ApplyResponse {
        val post = studyPostRepository.findByIdWithAuthorForUpdate(postId)
            ?: throw CustomException(ErrorCode.POST_NOT_FOUND)
        post.validateOpen()
        post.validateNotAuthor(userId)

        post.validateNotDuplicateApply(applyRepository.existsByPostIdAndApplicantId(postId, userId))
        val applicant = userRepository.findById(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        val apply = Apply.create(post, applicant, request.message, eventPublisher)
        applyRepository.save(apply)
        return ApplyResponse.from(apply)
    }
}
