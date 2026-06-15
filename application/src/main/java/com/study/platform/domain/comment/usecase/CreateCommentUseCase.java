package com.study.platform.domain.comment.usecase;

import com.study.platform.domain.comment.dto.request.CommentCreateRequest;
import com.study.platform.domain.comment.dto.response.CommentResponse;

import java.util.UUID;

public interface CreateCommentUseCase {
    CommentResponse execute(UUID userId, UUID postId, CommentCreateRequest request);
}
