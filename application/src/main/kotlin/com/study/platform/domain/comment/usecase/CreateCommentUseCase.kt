package com.study.platform.domain.comment.usecase

import com.study.platform.domain.comment.dto.request.CommentCreateRequest
import com.study.platform.domain.comment.dto.response.CommentResponse
import java.util.UUID

interface CreateCommentUseCase {
    fun execute(userId: UUID, postId: UUID, request: CommentCreateRequest): CommentResponse
}
