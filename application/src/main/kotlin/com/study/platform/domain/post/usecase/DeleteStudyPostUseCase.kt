package com.study.platform.domain.post.usecase

import java.util.UUID

interface DeleteStudyPostUseCase {
    fun execute(userId: UUID, postId: UUID)
}
