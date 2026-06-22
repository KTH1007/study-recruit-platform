package com.study.platform.support.fake

import com.study.platform.domain.comment.dto.response.CommentResponse
import com.study.platform.domain.comment.port.CommentQueryPort
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.UUID

class FakeCommentQueryPort(
    private val repository: FakeCommentRepository
) : CommentQueryPort {

    override fun findAllByPostId(postId: UUID, pageable: Pageable): Page<CommentResponse> {
        val list = repository.findAllByPostId(postId).map { CommentResponse.from(it) }
        return PageImpl(list, pageable, list.size.toLong())
    }
}
