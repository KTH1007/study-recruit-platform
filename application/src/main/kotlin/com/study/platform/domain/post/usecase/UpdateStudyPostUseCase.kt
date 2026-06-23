package com.study.platform.domain.post.usecase

import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest
import com.study.platform.domain.post.dto.response.StudyPostResponse
import java.util.UUID

interface UpdateStudyPostUseCase {
    fun execute(userId: UUID, postId: UUID, request: StudyPostUpdateRequest): StudyPostResponse
}
