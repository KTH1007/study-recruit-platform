package com.study.platform.domain.post.usecase;

import com.study.platform.domain.post.dto.request.StudyPostCreateRequest;
import com.study.platform.domain.post.dto.response.StudyPostResponse;

import java.util.UUID;

public interface CreateStudyPostUseCase {
    StudyPostResponse execute(UUID userId, StudyPostCreateRequest request);
}
