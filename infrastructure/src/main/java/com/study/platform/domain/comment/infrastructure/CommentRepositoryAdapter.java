package com.study.platform.domain.comment.infrastructure;

import com.study.platform.domain.comment.model.Comment;
import com.study.platform.domain.comment.model.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryAdapter implements CommentRepository {

    private final CommentJpaRepository commentJpaRepository;

    @Override
    public Comment save(Comment comment) {
        return commentJpaRepository.save(comment);
    }

    @Override
    public void delete(Comment comment) {
        commentJpaRepository.delete(comment);
    }

    @Override
    public Page<Comment> findAllByPostIdWithAuthor(UUID postId, Pageable pageable) {
        return commentJpaRepository.findAllByPostIdWithAuthor(postId, pageable);
    }

    @Override
    public Optional<Comment> findByIdWithAuthor(UUID commentId) {
        return commentJpaRepository.findByIdWithAuthor(commentId);
    }
}
