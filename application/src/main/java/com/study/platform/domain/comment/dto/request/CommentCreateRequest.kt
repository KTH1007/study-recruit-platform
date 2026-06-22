package com.study.platform.domain.comment.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "댓글 작성 요청")
data class CommentCreateRequest @JsonCreator constructor(

    @JsonProperty("content")
    @field:Schema(description = "댓글 내용", example = "안녕하세요")
    @field:NotBlank(message = "댓글 내용을 입력해주세요.")
    @field:Size(max = 500, message = "댓글은 500자 이하로 입력해주세요.")
    val content: String
)
