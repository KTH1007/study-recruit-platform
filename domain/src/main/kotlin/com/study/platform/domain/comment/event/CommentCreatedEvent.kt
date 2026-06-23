package com.study.platform.domain.comment.event

import java.util.UUID

data class CommentCreatedEvent(
    val postId: UUID,
    val authorId: UUID,
    val commenterId: UUID,
    val postTitle: String
)
