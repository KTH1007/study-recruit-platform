package com.study.platform.domain.apply.event

import java.util.UUID

data class ApplyReceivedEvent(
    val postId: UUID,
    val authorId: UUID,
    val postTitle: String
)
