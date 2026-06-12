package com.study.platform.domain.comment.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository {

    Comment save(Comment comment);
    void delete(Comment comment);
    Page<Comment> findAllByPostIdWithAuthor(UUID postId, Pageable pageable);
    Optional<Comment> findByIdWithAuthor(UUID commentId);
}
