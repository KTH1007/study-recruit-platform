package com.study.platform.domain.comment.infrastructure

import com.study.platform.domain.comment.model.Comment
import com.study.platform.domain.comment.model.CommentRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class CommentRepositoryAdapter(
    private val commentJpaRepository: CommentJpaRepository
) : CommentRepository {

    override fun save(comment: Comment): Comment =
        commentJpaRepository.save(comment)

    override fun delete(comment: Comment) {
        commentJpaRepository.delete(comment)
    }

    override fun deleteAllByPostId(postId: UUID) {
        commentJpaRepository.deleteAllByPostId(postId)
    }

    override fun findById(commentId: UUID): Comment? =
        commentJpaRepository.findById(commentId).orElse(null)

    override fun findByIdWithAuthor(commentId: UUID): Comment? =
        commentJpaRepository.findByIdWithAuthor(commentId)
}
