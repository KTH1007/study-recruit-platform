package com.study.platform.domain.chat.infrastructure

import com.study.platform.domain.chat.model.ChatMessage
import com.study.platform.domain.chat.model.ChatMessageRepository
import org.springframework.stereotype.Repository

@Repository
class ChatMessageRepositoryAdapter(
    private val chatMessageJpaRepository: ChatMessageJpaRepository
) : ChatMessageRepository {

    override fun save(message: ChatMessage): ChatMessage =
        chatMessageJpaRepository.save(message)
}
