package com.study.platform.domain.apply.usecase

import com.study.platform.domain.apply.dto.response.ApplyResponse
import java.util.UUID

interface FindAppliesUseCase {
    fun execute(userId: UUID, postId: UUID): List<ApplyResponse>
}
