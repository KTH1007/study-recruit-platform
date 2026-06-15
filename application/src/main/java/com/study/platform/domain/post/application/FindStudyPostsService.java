package com.study.platform.domain.post.application;

import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse;
import com.study.platform.domain.post.model.StudyPostStatus;
import com.study.platform.domain.post.port.StudyPostQueryPort;
import com.study.platform.domain.post.usecase.FindStudyPostsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindStudyPostsService implements FindStudyPostsUseCase {

    private final StudyPostQueryPort studyPostQueryPort;

    @Override
    public Page<StudyPostSummaryResponse> execute(String techStack, StudyPostStatus status, Pageable pageable) {
        return studyPostQueryPort.findAllWithFilter(techStack, status, pageable);
    }
}
