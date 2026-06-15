package com.study.platform.domain.post.usecase;

import com.study.platform.domain.post.dto.response.StudyPostResponse;

import java.util.UUID;

public interface FindStudyPostUseCase {
    StudyPostResponse execute(UUID postId);
}
