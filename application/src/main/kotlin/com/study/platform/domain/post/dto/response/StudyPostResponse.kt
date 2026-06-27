package com.study.platform.domain.post.dto.response

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class StudyPostResponse @JsonCreator constructor(

    @JsonProperty("id")
    @field:Schema(description = "게시글 ID")
    val id: UUID,

    @JsonProperty("authorId")
    @field:Schema(description = "작성자 ID")
    val authorId: UUID,

    @JsonProperty("authorNickname")
    @field:Schema(description = "작성자 닉네임")
    val authorNickname: String,

    @JsonProperty("title")
    @field:Schema(description = "제목")
    val title: String,

    @JsonProperty("description")
    @field:Schema(description = "내용")
    val description: String,

    @JsonProperty("techStack")
    @field:Schema(description = "기술스택")
    val techStack: String?,

    @JsonProperty("maxMembers")
    @field:Schema(description = "최대 인원")
    val maxMembers: Int,

    @JsonProperty("deadline")
    @field:Schema(description = "모집 마감일")
    val deadline: LocalDateTime,

    @JsonProperty("status")
    @field:Schema(description = "모집 상태")
    val status: StudyPostStatus,

    @JsonProperty("createdAt")
    @field:Schema(description = "작성일")
    val createdAt: LocalDateTime,

    @JsonProperty("updatedAt")
    @field:Schema(description = "수정일")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(post: StudyPost): StudyPostResponse = StudyPostResponse(
            id = post.id!!,
            authorId = post.author!!.id!!,
            authorNickname = post.author!!.nickname,
            title = post.title,
            description = post.description,
            techStack = post.techStack,
            maxMembers = post.maxMembers,
            deadline = post.deadline!!,
            status = post.status,
            createdAt = post.createdAt!!,
            updatedAt = post.updatedAt!!
        )
    }
}
