package com.study.platform.support.fake

import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.domain.apply.port.ApplyQueryPort
import java.util.UUID

class FakeApplyQueryPort(
    private val repository: FakeApplyRepository
) : ApplyQueryPort {

    override fun findAllByPostId(postId: UUID): List<ApplyResponse> =
        repository.findAllByPostId(postId).map { ApplyResponse.from(it) }
}
