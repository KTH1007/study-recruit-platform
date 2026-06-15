package com.study.platform.domain.chat.infrastructure;

import com.study.platform.domain.chat.model.ChatMessage;
import com.study.platform.domain.chat.model.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryAdapter implements ChatMessageRepository {

    private final ChatMessageJpaRepository chatMessageJpaRepository;

    @Override
    public ChatMessage save(ChatMessage message) {
        return chatMessageJpaRepository.save(message);
    }
}
