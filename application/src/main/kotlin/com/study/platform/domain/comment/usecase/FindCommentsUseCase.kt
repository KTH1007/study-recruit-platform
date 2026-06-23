package com.study.platform.domain.comment.usecase

import com.study.platform.domain.comment.dto.response.CommentResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface FindCommentsUseCase {
    fun execute(postId: UUID, pageable: Pageable): Page<CommentResponse>
}
