package com.study.platform.domain.chat.model

import java.util.UUID

interface ChatMessageRepository {
    fun save(message: ChatMessage): ChatMessage
    fun deleteAllByTeamId(teamId: UUID)
}
