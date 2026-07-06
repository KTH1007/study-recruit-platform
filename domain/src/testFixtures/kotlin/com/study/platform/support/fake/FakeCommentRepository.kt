package com.study.platform.support.fake

import com.study.platform.domain.comment.model.Comment
import com.study.platform.domain.comment.model.CommentRepository
import java.util.UUID

class FakeCommentRepository : AbstractFakeUuidRepository<Comment>(), CommentRepository {

    override fun idOf(entity: Comment): UUID? = entity.id
    override fun hasTimestamps() = true

    override fun save(comment: Comment): Comment = saveEntity(comment)

    override fun delete(comment: Comment) = deleteEntity(comment)

    override fun deleteAllByPostId(postId: UUID) {
        findAllByPostId(postId).mapNotNull { it.id }.forEach { store.remove(it) }
    }

    override fun findByIdWithAuthor(commentId: UUID): Comment? = store[commentId]

    fun findAllByPostId(postId: UUID): List<Comment> =
        store.values.filter { c -> c.post?.id == postId }
}
