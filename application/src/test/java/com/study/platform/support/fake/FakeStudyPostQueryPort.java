package com.study.platform.support.fake;

import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse;
import com.study.platform.domain.post.model.StudyPostStatus;
import com.study.platform.domain.post.port.StudyPostQueryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class FakeStudyPostQueryPort implements StudyPostQueryPort {

    @Override
    public Page<StudyPostSummaryResponse> findAllWithFilter(String techStack, StudyPostStatus status, Pageable pageable) {
        return new PageImpl<>(List.of(), pageable, 0);
    }
}
