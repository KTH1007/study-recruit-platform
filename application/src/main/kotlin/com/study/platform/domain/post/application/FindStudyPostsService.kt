package com.study.platform.domain.post.application

import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse
import com.study.platform.domain.post.model.StudyPostStatus
import com.study.platform.domain.post.port.StudyPostQueryPort
import com.study.platform.domain.post.usecase.FindStudyPostsUseCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FindStudyPostsService(
    private val studyPostQueryPort: StudyPostQueryPort
) : FindStudyPostsUseCase {

    override fun execute(techStack: String?, status: StudyPostStatus?, pageable: Pageable): Page<StudyPostSummaryResponse> {
        return studyPostQueryPort.findAllWithFilter(techStack, status, pageable)
    }
}
