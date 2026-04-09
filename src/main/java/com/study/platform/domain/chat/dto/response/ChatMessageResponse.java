package com.study.platform.domain.chat.dto.response;

import com.study.platform.domain.chat.model.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "채팅 메시지 응답")
public record ChatMessageResponse(

        @Schema(description = "메시지 ID")
        UUID messageId,

        @Schema(description = "팀 ID")
        UUID teamId,

        @Schema(description = "보낸 사람 ID")
        UUID senderId,

        @Schema(description = "보낸 사람 닉네임")
        String senderNickname,

        @Schema(description = "채팅 내용")
        String content,

        @Schema(description = "전송 시각")
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getTeam().getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}