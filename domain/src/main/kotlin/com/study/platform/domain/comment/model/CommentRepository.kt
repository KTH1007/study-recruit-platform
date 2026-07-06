package com.study.platform.domain.comment.model

import java.util.UUID

interface CommentRepository {

    fun save(comment: Comment): Comment
    fun delete(comment: Comment)
    fun deleteAllByPostId(postId: UUID)
    fun findById(commentId: UUID): Comment?
    fun findByIdWithAuthor(commentId: UUID): Comment?
}
