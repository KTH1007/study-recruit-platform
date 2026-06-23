package com.study.platform.support.fake

import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse
import com.study.platform.domain.post.model.StudyPostStatus
import com.study.platform.domain.post.port.StudyPostQueryPort
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class FakeStudyPostQueryPort : StudyPostQueryPort {

    override fun findAllWithFilter(techStack: String?, status: StudyPostStatus?, pageable: Pageable): Page<StudyPostSummaryResponse> =
        PageImpl(emptyList(), pageable, 0)
}
