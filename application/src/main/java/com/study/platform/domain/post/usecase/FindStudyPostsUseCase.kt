package com.study.platform.domain.post.usecase

import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse
import com.study.platform.domain.post.model.StudyPostStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface FindStudyPostsUseCase {
    fun execute(techStack: String?, status: StudyPostStatus?, pageable: Pageable): Page<StudyPostSummaryResponse>
}
