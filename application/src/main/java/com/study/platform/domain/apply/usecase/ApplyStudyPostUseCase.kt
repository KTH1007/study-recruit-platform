package com.study.platform.domain.apply.usecase

import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import com.study.platform.domain.apply.dto.response.ApplyResponse
import java.util.UUID

interface ApplyStudyPostUseCase {
    fun execute(userId: UUID, postId: UUID, request: ApplyCreateRequest): ApplyResponse
}
