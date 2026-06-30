package com.study.platform.domain.apply.port

import com.study.platform.domain.apply.dto.response.ApplyResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ApplyQueryPort {
    fun findAllByPostId(postId: UUID, pageable: Pageable): Page<ApplyResponse>
}
