package com.study.platform.domain.post.usecase;

import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest;
import com.study.platform.domain.post.dto.response.StudyPostResponse;

import java.util.UUID;

public interface UpdateStudyPostUseCase {
    StudyPostResponse execute(UUID userId, UUID postId, StudyPostUpdateRequest request);
}
