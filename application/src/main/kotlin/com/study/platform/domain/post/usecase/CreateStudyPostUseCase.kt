package com.study.platform.domain.post.usecase

import com.study.platform.domain.post.dto.request.StudyPostCreateRequest
import com.study.platform.domain.post.dto.response.StudyPostResponse
import java.util.UUID

interface CreateStudyPostUseCase {
    fun execute(userId: UUID, request: StudyPostCreateRequest): StudyPostResponse
}
