package com.study.platform.domain.comment.application;

import com.study.platform.domain.comment.dto.response.CommentResponse;
import com.study.platform.domain.comment.port.CommentQueryPort;
import com.study.platform.domain.comment.usecase.FindCommentsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindCommentsService implements FindCommentsUseCase {

    private final CommentQueryPort commentQueryPort;

    @Override
    public Page<CommentResponse> execute(UUID postId, Pageable pageable) {
        return commentQueryPort.findAllByPostId(postId, pageable);
    }
}
