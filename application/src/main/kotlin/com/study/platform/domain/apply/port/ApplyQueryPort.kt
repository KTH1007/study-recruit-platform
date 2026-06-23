package com.study.platform.domain.apply.port

import com.study.platform.domain.apply.dto.response.ApplyResponse
import java.util.UUID

interface ApplyQueryPort {
    fun findAllByPostId(postId: UUID): List<ApplyResponse>
}
