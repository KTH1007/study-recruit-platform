package com.study.platform.domain.comment.usecase;

import com.study.platform.domain.comment.dto.request.CommentUpdateRequest;
import com.study.platform.domain.comment.dto.response.CommentResponse;

import java.util.UUID;

public interface UpdateCommentUseCase {
    CommentResponse execute(UUID userId, UUID commentId, CommentUpdateRequest request);
}
