package com.study.platform.support.fake

import com.study.platform.domain.chat.dto.response.ChatMessageResponse
import com.study.platform.domain.chat.port.ChatQueryPort
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import java.util.UUID

class FakeChatQueryPort(
    private val repository: FakeChatMessageRepository
) : ChatQueryPort {

    override fun findMessagesByTeamId(teamId: UUID, pageable: Pageable): Slice<ChatMessageResponse> {
        val all = repository.findAllByTeamId(teamId)
            .sortedByDescending { it.createdAt }
            .map { ChatMessageResponse.from(it) }
        val content = all.drop(pageable.offset.toInt()).take(pageable.pageSize)
        val hasNext = pageable.offset + content.size < all.size
        return SliceImpl(content, pageable, hasNext)
    }
}
