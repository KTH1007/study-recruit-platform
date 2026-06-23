package com.study.platform.domain.comment.application

import com.study.platform.domain.comment.dto.response.CommentResponse
import com.study.platform.domain.comment.port.CommentQueryPort
import com.study.platform.domain.comment.usecase.FindCommentsUseCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FindCommentsService(
    private val commentQueryPort: CommentQueryPort
) : FindCommentsUseCase {

    override fun execute(postId: UUID, pageable: Pageable): Page<CommentResponse> {
        return commentQueryPort.findAllByPostId(postId, pageable)
    }
}
