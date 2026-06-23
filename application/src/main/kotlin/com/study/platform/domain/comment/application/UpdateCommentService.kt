package com.study.platform.domain.comment.application

import com.study.platform.domain.comment.dto.request.CommentUpdateRequest
import com.study.platform.domain.comment.dto.response.CommentResponse
import com.study.platform.domain.comment.model.CommentRepository
import com.study.platform.domain.comment.usecase.UpdateCommentUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UpdateCommentService(
    private val commentRepository: CommentRepository
) : UpdateCommentUseCase {

    @Transactional
    override fun execute(userId: UUID, commentId: UUID, request: CommentUpdateRequest): CommentResponse {
        val comment = commentRepository.findByIdWithAuthor(commentId)
            ?: throw CustomException(ErrorCode.COMMENT_NOT_FOUND)
        comment.validateAuthor(userId)
        comment.update(request.content)
        return CommentResponse.from(comment)
    }
}
