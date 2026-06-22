package com.study.platform.domain.post.usecase

import com.study.platform.domain.post.dto.response.StudyPostResponse
import java.util.UUID

interface CloseStudyPostUseCase {
    fun execute(userId: UUID, postId: UUID): StudyPostResponse
}
