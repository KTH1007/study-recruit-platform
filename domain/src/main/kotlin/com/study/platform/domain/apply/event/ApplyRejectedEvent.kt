package com.study.platform.domain.apply.event

import java.util.UUID

data class ApplyRejectedEvent(
    val postId: UUID,
    val applicantId: UUID,
    val postTitle: String
)
