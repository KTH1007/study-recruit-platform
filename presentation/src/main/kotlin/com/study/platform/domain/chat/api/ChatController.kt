package com.study.platform.domain.chat.api

import com.study.platform.domain.chat.dto.request.ChatMessageRequest
import com.study.platform.domain.chat.usecase.SaveAndPublishChatMessageUseCase
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Controller
import java.security.Principal
import java.util.UUID

@Controller
class ChatController(
    private val saveAndPublishChatMessageUseCase: SaveAndPublishChatMessageUseCase
) {

    @MessageMapping("/chat/{teamId}")
    fun sendMessage(
        @DestinationVariable teamId: UUID,
        request: ChatMessageRequest,
        principal: Principal
    ) {
        val userId = (principal as UsernamePasswordAuthenticationToken).principal as UUID
        saveAndPublishChatMessageUseCase.execute(userId, teamId, request)
    }
}
