package com.study.platform.domain.comment.event

import java.util.UUID

data class MentionEvent(
    val postId: UUID,
    val mentionedUserId: UUID,
    val commenterNickname: String,
    val postTitle: String
)
