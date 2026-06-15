package com.study.platform.domain.chat.usecase;

import com.study.platform.domain.chat.dto.response.ChatMessageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface FindChatMessagesUseCase {
    Slice<ChatMessageResponse> execute(UUID userId, UUID teamId, Pageable pageable);
}
