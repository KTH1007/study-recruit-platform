package com.study.platform.domain.chat.infrastructure

import com.study.platform.domain.chat.dto.response.ChatMessageResponse
import com.study.platform.domain.chat.port.ChatQueryPort
import com.study.platform.global.jooq.JooqUtils.binToUuid
import com.study.platform.global.jooq.JooqUtils.uuidEq
import com.study.platform.jooq.tables.references.CHAT_MESSAGES
import com.study.platform.jooq.tables.references.USERS
import org.jooq.DSLContext
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class ChatQueryRepositoryAdapter(
    private val dsl: DSLContext
) : ChatQueryPort {

    override fun findMessagesByTeamId(teamId: UUID, pageable: Pageable): Slice<ChatMessageResponse> {
        val fetchSize = pageable.pageSize + 1
        val m = CHAT_MESSAGES.`as`("m")
        val u = USERS.`as`("u")

        val result = dsl.select(
            binToUuid(m.ID).`as`("messageId"),
            binToUuid(m.TEAM_ID).`as`("teamId"),
            binToUuid(m.SENDER_ID).`as`("senderId"),
            u.NICKNAME.`as`("senderNickname"),
            m.CONTENT.`as`("content"),
            m.CREATED_AT.`as`("createdAt")
        )
            .from(m)
            .join(u).on(m.SENDER_ID.eq(u.ID))
            .where(uuidEq(m.TEAM_ID, teamId))
            .orderBy(m.CREATED_AT.desc())
            .limit(fetchSize)
            .offset(pageable.offset)
            .fetch { r ->
                ChatMessageResponse(
                    UUID.fromString(r.get("messageId", String::class.java)),
                    UUID.fromString(r.get("teamId", String::class.java)),
                    UUID.fromString(r.get("senderId", String::class.java)),
                    r.get("senderNickname", String::class.java),
                    r.get("content", String::class.java),
                    r.get("createdAt", LocalDateTime::class.java)
                )
            }

        val hasNext = result.size == fetchSize
        val content = if (hasNext) result.subList(0, pageable.pageSize) else result
        return SliceImpl(content, pageable, hasNext)
    }
}
