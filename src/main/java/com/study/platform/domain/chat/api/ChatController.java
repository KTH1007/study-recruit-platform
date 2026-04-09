package com.study.platform.domain.chat.api;

import com.study.platform.domain.chat.application.ChatService;
import com.study.platform.domain.chat.dto.request.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat/{teamId}")
    public void sendMessage(
            @DestinationVariable UUID teamId,
            ChatMessageRequest request,
            Principal principal
    ) {
        UUID userId = (UUID) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        chatService.saveAndPublish(userId, teamId, request);
    }
}
