package com.study.platform.domain.post.model

import java.time.LocalDateTime
import java.util.UUID

data class PostSearchResult(
    val id: UUID,
    val authorNickname: String,
    val title: String,
    val techStack: String?,
    val maxMembers: Int,
    val deadline: LocalDateTime?,
    val status: StudyPostStatus,
    val createdAt: LocalDateTime
)
