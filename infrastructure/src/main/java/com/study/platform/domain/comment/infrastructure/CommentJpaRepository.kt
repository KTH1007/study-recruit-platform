package com.study.platform.domain.comment.infrastructure

import com.study.platform.domain.comment.model.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CommentJpaRepository : JpaRepository<Comment, UUID> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.id = :commentId")
    fun findByIdWithAuthor(@Param("commentId") commentId: UUID): Comment?
}
