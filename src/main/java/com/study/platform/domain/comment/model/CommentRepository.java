package com.study.platform.domain.comment.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query(value = "SELECT c FROM Comment c " +
            "JOIN FETCH c.author WHERE c.post.id = :postId ORDER BY c. createdAt ASC",
            countQuery = "SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    Page<Comment> findAllByPostIdWithAuthor(@Param("postId") UUID postId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.id = :commentId")
    Optional<Comment> findByIdWithAuthor(@Param("commentId") UUID commentId);
}
