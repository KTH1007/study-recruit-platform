package com.study.platform.domain.chat.infrastructure;

import com.study.platform.domain.chat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessage, UUID> {
}
