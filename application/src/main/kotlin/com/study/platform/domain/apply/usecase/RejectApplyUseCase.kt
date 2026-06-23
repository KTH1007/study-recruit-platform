package com.study.platform.domain.apply.usecase

import com.study.platform.domain.apply.dto.response.ApplyResponse
import java.util.UUID

interface RejectApplyUseCase {
    fun execute(userId: UUID, applyId: UUID): ApplyResponse
}
