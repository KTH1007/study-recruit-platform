package com.study.platform.domain.chat.dto.response

import com.study.platform.domain.chat.model.ChatMessage
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

@Schema(description = "채팅 메시지 응답")
data class ChatMessageResponse(

    @field:Schema(description = "메시지 ID")
    val messageId: UUID,

    @field:Schema(description = "팀 ID")
    val teamId: UUID,

    @field:Schema(description = "보낸 사람 ID")
    val senderId: UUID,

    @field:Schema(description = "보낸 사람 닉네임")
    val senderNickname: String,

    @field:Schema(description = "채팅 내용")
    val content: String,

    @field:Schema(description = "전송 시각")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(message: ChatMessage): ChatMessageResponse = ChatMessageResponse(
            messageId = message.id!!,
            teamId = message.team!!.id!!,
            senderId = message.sender!!.id!!,
            senderNickname = message.sender!!.nickname,
            content = message.content,
            createdAt = message.createdAt!!
        )
    }
}
