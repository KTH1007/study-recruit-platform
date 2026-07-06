package com.study.platform.domain.comment.application

import com.study.platform.domain.comment.model.CommentRepository
import com.study.platform.domain.comment.usecase.DeleteCommentUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteCommentService(
    private val commentRepository: CommentRepository
) : DeleteCommentUseCase {

    @Transactional
    override fun execute(userId: UUID, commentId: UUID) {
        val comment = commentRepository.findById(commentId)
            ?: throw CustomException(ErrorCode.COMMENT_NOT_FOUND)
        comment.validateAuthor(userId)
        commentRepository.delete(comment)
    }
}
