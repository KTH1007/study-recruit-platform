package com.study.platform.support.fake

import com.study.platform.domain.comment.model.Comment
import com.study.platform.domain.comment.model.CommentRepository
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.UUID

class FakeCommentRepository : CommentRepository {

    private val store: MutableMap<UUID, Comment> = HashMap()

    override fun save(comment: Comment): Comment {
        if (comment.id == null) {
            ReflectionTestUtils.setField(comment, "id", UUID.randomUUID())
        }
        if (comment.createdAt == null) {
            ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.now())
        }
        ReflectionTestUtils.setField(comment, "updatedAt", LocalDateTime.now())
        store[comment.id!!] = comment
        return comment
    }

    override fun delete(comment: Comment) {
        store.remove(comment.id)
    }

    override fun findByIdWithAuthor(commentId: UUID): Comment? = store[commentId]

    fun findAllByPostId(postId: UUID): List<Comment> =
        store.values.filter { c -> c.post?.id == postId }
}
