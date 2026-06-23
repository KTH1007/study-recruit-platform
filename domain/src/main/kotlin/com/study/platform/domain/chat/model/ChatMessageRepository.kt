package com.study.platform.domain.chat.model

interface ChatMessageRepository {
    fun save(message: ChatMessage): ChatMessage
}
