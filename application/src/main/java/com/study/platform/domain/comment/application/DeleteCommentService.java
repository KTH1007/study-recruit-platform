package com.study.platform.domain.comment.application;

import com.study.platform.domain.comment.model.Comment;
import com.study.platform.domain.comment.model.CommentRepository;
import com.study.platform.domain.comment.usecase.DeleteCommentUseCase;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCommentService implements DeleteCommentUseCase {

    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public void execute(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findByIdWithAuthor(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        comment.validateAuthor(userId);
        commentRepository.delete(comment);
    }
}
