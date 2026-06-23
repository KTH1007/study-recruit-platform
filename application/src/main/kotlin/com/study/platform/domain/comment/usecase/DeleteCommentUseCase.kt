package com.study.platform.domain.comment.usecase

import java.util.UUID

interface DeleteCommentUseCase {
    fun execute(userId: UUID, commentId: UUID)
}
