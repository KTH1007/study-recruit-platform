package com.study.platform.domain.chat.infrastructure;

import com.study.platform.domain.chat.model.ChatMessage;
import com.study.platform.domain.chat.model.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryAdapter implements ChatMessageRepository {

    private final ChatMessageJpaRepository chatMessageJpaRepository;

    @Override
    public ChatMessage save(ChatMessage message) {
        return chatMessageJpaRepository.save(message);
    }

    @Override
    public Slice<ChatMessage> findByTeamIdWithSender(UUID teamId, Pageable pageable) {
        return chatMessageJpaRepository.findByTeamIdWithSender(teamId, pageable);
    }
}
