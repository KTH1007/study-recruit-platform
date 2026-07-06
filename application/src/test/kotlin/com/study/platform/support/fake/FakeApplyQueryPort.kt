package com.study.platform.support.fake

import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.domain.apply.port.ApplyQueryPort
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.UUID

class FakeApplyQueryPort(
    private val repository: FakeApplyRepository
) : ApplyQueryPort {

    override fun findAllByPostId(postId: UUID, pageable: Pageable): Page<ApplyResponse> {
        val all = repository.findAllByPostId(postId)
            .sortedByDescending { it.createdAt }
            .map { ApplyResponse.from(it) }
        val content = all.drop(pageable.offset.toInt()).take(pageable.pageSize)
        return PageImpl(content, pageable, all.size.toLong())
    }
}
