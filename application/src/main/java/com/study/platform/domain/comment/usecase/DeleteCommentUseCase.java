package com.study.platform.domain.comment.usecase;

import java.util.UUID;

public interface DeleteCommentUseCase {
    void execute(UUID userId, UUID commentId);
}
