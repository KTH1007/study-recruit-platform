package com.study.platform.domain.chat.infrastructure

import com.study.platform.domain.chat.dto.response.ChatMessageResponse
import com.study.platform.domain.chat.port.ChatQueryPort
import com.study.platform.global.jooq.JooqUtils
import org.jooq.DSLContext
import org.jooq.impl.DSL
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

        val result = dsl
            .select(
                DSL.field("BIN_TO_UUID(m.id)").`as`("messageId"),
                DSL.field("BIN_TO_UUID(m.team_id)").`as`("teamId"),
                DSL.field("BIN_TO_UUID(m.sender_id)").`as`("senderId"),
                DSL.field("u.nickname").`as`("senderNickname"),
                DSL.field("m.content").`as`("content"),
                DSL.field("m.created_at").`as`("createdAt")
            )
            .from(DSL.table("chat_messages").`as`("m"))
            .join(DSL.table("users").`as`("u"))
            .on(DSL.field("m.sender_id").eq(DSL.field("u.id")))
            .where(JooqUtils.uuidEq("m.team_id", teamId))
            .orderBy(DSL.field("m.created_at").desc())
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
