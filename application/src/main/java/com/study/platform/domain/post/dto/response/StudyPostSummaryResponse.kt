package com.study.platform.domain.post.dto.response

import com.study.platform.domain.post.model.PostSearchResult
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class StudyPostSummaryResponse(

    @field:Schema(description = "게시글 ID")
    val id: UUID,

    @field:Schema(description = "작성자 닉네임")
    val authorNickname: String,

    @field:Schema(description = "제목")
    val title: String,

    @field:Schema(description = "기술스택")
    val techStack: String?,

    @field:Schema(description = "최대 인원")
    val maxMembers: Int,

    @field:Schema(description = "모집 마감일")
    val deadline: LocalDateTime,

    @field:Schema(description = "모집 상태")
    val status: StudyPostStatus,

    @field:Schema(description = "작성일")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(post: StudyPost): StudyPostSummaryResponse = StudyPostSummaryResponse(
            id = post.id!!,
            authorNickname = post.author!!.nickname,
            title = post.title,
            techStack = post.techStack,
            maxMembers = post.maxMembers,
            deadline = post.deadline!!,
            status = post.status,
            createdAt = post.createdAt!!
        )

        fun from(result: PostSearchResult): StudyPostSummaryResponse = StudyPostSummaryResponse(
            id = result.id,
            authorNickname = result.authorNickname,
            title = result.title,
            techStack = result.techStack,
            maxMembers = result.maxMembers,
            deadline = result.deadline!!,
            status = result.status,
            createdAt = result.createdAt
        )
    }
}
