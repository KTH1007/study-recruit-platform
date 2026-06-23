package com.study.platform.domain.chat.model

interface ChatPublisher {
    fun publish(message: ChatMessage)
}
