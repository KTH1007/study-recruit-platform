package com.study.platform.domain.post.usecase;

import java.util.UUID;

public interface DeleteStudyPostUseCase {
    void execute(UUID userId, UUID postId);
}
