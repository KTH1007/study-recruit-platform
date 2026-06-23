package com.study.platform.domain.comment.model

import com.study.platform.domain.comment.event.CommentCreatedEvent
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.user.model.User
import com.study.platform.global.entity.BaseTimeEntity
import com.study.platform.global.event.DomainEventPublisher
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "comments",
    indexes = [Index(name = "idx_comment_post_created", columnList = "post_id, created_at")]
)
class Comment : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    var post: StudyPost? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: User? = null

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String = ""

    fun update(content: String) {
        this.content = content
    }

    fun isAuthor(userId: UUID): Boolean = author?.id == userId

    fun validateAuthor(userId: UUID) {
        if (!isAuthor(userId)) throw CustomException(ErrorCode.NOT_COMMENT_AUTHOR)
    }

    companion object {
        fun create(post: StudyPost, author: User, content: String, publisher: DomainEventPublisher): Comment {
            val comment = Comment().also {
                it.post = post
                it.author = author
                it.content = content
            }
            if (!post.isAuthor(author.id!!)) {
                publisher.publish(CommentCreatedEvent(post.id!!, post.author!!.id!!, author.id!!, post.title))
            }
            return comment
        }
    }
}
