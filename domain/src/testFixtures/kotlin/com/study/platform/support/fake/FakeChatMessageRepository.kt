package com.study.platform.support.fake

import com.study.platform.domain.chat.model.ChatMessage
import com.study.platform.domain.chat.model.ChatMessageRepository
import java.util.UUID

class FakeChatMessageRepository : AbstractFakeUuidRepository<ChatMessage>(), ChatMessageRepository {

    override fun idOf(entity: ChatMessage): UUID? = entity.id
    override fun hasTimestamps() = true

    override fun save(message: ChatMessage): ChatMessage = saveEntity(message)

    fun findAllByTeamId(teamId: UUID): List<ChatMessage> =
        store.values.filter { m -> m.team?.id == teamId }
}
