package com.study.platform.domain.comment.usecase

import com.study.platform.domain.comment.dto.request.CommentUpdateRequest
import com.study.platform.domain.comment.dto.response.CommentResponse
import java.util.UUID

interface UpdateCommentUseCase {
    fun execute(userId: UUID, commentId: UUID, request: CommentUpdateRequest): CommentResponse
}
