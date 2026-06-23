package com.study.platform.domain.post.event

import java.util.UUID

data class PostDeadlineReminderEvent(
    val postId: UUID,
    val authorId: UUID,
    val postTitle: String
)
