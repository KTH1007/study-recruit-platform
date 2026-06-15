package com.study.platform.domain.comment.model;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository {

    Comment save(Comment comment);
    void delete(Comment comment);
    Optional<Comment> findByIdWithAuthor(UUID commentId);
}
