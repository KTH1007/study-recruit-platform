package com.study.platform.domain.chat.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "채팅 메시지 전송 요청")
data class ChatMessageRequest @JsonCreator constructor(

    @JsonProperty("content")
    @field:Schema(description = "채팅 내용", example = "안녕하세요")
    @field:NotBlank(message = "채팅 내용은 필수입니다.")
    val content: String
)
