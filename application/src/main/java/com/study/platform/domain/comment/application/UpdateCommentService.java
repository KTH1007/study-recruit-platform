package com.study.platform.domain.comment.application;

import com.study.platform.domain.comment.dto.request.CommentUpdateRequest;
import com.study.platform.domain.comment.dto.response.CommentResponse;
import com.study.platform.domain.comment.model.Comment;
import com.study.platform.domain.comment.model.CommentRepository;
import com.study.platform.domain.comment.usecase.UpdateCommentUseCase;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCommentService implements UpdateCommentUseCase {

    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentResponse execute(UUID userId, UUID commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findByIdWithAuthor(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        comment.validateAuthor(userId);
        comment.update(request.content());
        return CommentResponse.from(comment);
    }
}
