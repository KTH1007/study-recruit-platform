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
        val list = repository.findAllByPostId(postId).map { ApplyResponse.from(it) }
        return PageImpl(list, pageable, list.size.toLong())
    }
}
