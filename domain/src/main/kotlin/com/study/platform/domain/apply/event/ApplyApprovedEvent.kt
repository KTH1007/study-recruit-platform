package com.study.platform.domain.apply.event

import java.util.UUID

data class ApplyApprovedEvent(
    val postId: UUID,
    val applicantId: UUID,
    val postTitle: String
)
