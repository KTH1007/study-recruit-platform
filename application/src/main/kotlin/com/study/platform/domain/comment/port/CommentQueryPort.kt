package com.study.platform.domain.comment.port

import com.study.platform.domain.comment.dto.response.CommentResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface CommentQueryPort {
    fun findAllByPostId(postId: UUID, pageable: Pageable): Page<CommentResponse>
}
