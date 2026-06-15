package com.study.platform.domain.chat.usecase;

import com.study.platform.domain.chat.dto.request.ChatMessageRequest;

import java.util.UUID;

public interface SaveAndPublishChatMessageUseCase {
    void execute(UUID userId, UUID teamId, ChatMessageRequest request);
}
