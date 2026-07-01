package com.study.platform.domain.apply.usecase

import com.study.platform.domain.apply.dto.response.ApplyResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface FindAppliesUseCase {
    fun execute(userId: UUID, postId: UUID, pageable: Pageable): Page<ApplyResponse>
}
