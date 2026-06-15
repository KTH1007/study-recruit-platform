package com.study.platform.domain.post.port;

import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse;
import com.study.platform.domain.post.model.StudyPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyPostQueryPort {

    Page<StudyPostSummaryResponse> findAllWithFilter(String techStack, StudyPostStatus status, Pageable pageable);
}
