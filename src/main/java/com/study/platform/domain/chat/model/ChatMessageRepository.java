package com.study.platform.domain.chat.model;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface ChatMessageRepository {

    ChatMessage save(ChatMessage message);
    Slice<ChatMessage> findByTeamIdWithSender(UUID teamId, Pageable pageable);
}
