package com.study.platform.domain.comment.dto.response

import com.study.platform.domain.comment.model.Comment
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

@Schema(description = "댓글 응답")
data class CommentResponse(

    @field:Schema(description = "댓글 ID")
    val id: UUID,

    @field:Schema(description = "작성자 ID")
    val authorId: UUID,

    @field:Schema(description = "작성자 닉네임")
    val authorNickname: String,

    @field:Schema(description = "댓글 내용")
    val content: String,

    @field:Schema(description = "작성 시각")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(comment: Comment): CommentResponse = CommentResponse(
            id = comment.id!!,
            authorId = comment.author!!.id!!,
            authorNickname = comment.author!!.nickname,
            content = comment.content,
            createdAt = comment.createdAt!!
        )
    }
}
