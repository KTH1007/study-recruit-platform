package com.study.platform.domain.chat.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "채팅 메시지 전송 요청")
data class ChatMessageRequest @JsonCreator constructor(

    @JsonProperty("content")
    @field:Schema(description = "채팅 내용", example = "안녕하세요")
    @field:NotBlank(message = "채팅 내용은 필수입니다.")
    @field:Size(max = 1000, message = "채팅 내용은 1000자 이하로 입력해주세요.")
    val content: String
)
