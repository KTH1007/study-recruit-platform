package com.study.platform.support.fake;

import com.study.platform.domain.comment.dto.response.CommentResponse;
import com.study.platform.domain.comment.port.CommentQueryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public class FakeCommentQueryPort implements CommentQueryPort {

    private final FakeCommentRepository repository;

    public FakeCommentQueryPort(FakeCommentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<CommentResponse> findAllByPostId(UUID postId, Pageable pageable) {
        List<CommentResponse> list = repository.findAllByPostId(postId).stream()
                .map(CommentResponse::from)
                .toList();
        return new PageImpl<>(list, pageable, list.size());
    }
}
