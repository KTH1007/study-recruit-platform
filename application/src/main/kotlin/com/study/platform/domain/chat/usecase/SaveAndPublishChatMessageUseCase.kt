package com.study.platform.domain.chat.usecase

import com.study.platform.domain.chat.dto.request.ChatMessageRequest
import java.util.UUID

interface SaveAndPublishChatMessageUseCase {
    fun execute(userId: UUID, teamId: UUID, request: ChatMessageRequest)
}
