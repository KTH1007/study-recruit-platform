package com.study.platform.domain.apply.usecase

import java.util.UUID

interface CancelApplyUseCase {
    fun execute(userId: UUID, postId: UUID)
}
