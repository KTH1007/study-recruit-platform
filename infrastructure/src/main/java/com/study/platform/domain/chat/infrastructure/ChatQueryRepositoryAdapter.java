package com.study.platform.domain.chat.infrastructure;

import com.study.platform.domain.chat.dto.response.ChatMessageResponse;
import com.study.platform.domain.chat.port.ChatQueryPort;
import com.study.platform.global.jooq.JooqUtils;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ChatQueryRepositoryAdapter implements ChatQueryPort {

    private final DSLContext dsl;

    @Override
    public Slice<ChatMessageResponse> findMessagesByTeamId(UUID teamId, Pageable pageable) {
        int fetchSize = pageable.getPageSize() + 1;

        List<ChatMessageResponse> result = dsl
                .select(
                        DSL.field("BIN_TO_UUID(m.id)").as("messageId"),
                        DSL.field("BIN_TO_UUID(m.team_id)").as("teamId"),
                        DSL.field("BIN_TO_UUID(m.sender_id)").as("senderId"),
                        DSL.field("u.nickname").as("senderNickname"),
                        DSL.field("m.content").as("content"),
                        DSL.field("m.created_at").as("createdAt")
                )
                .from(DSL.table("chat_messages").as("m"))
                .join(DSL.table("users").as("u"))
                .on(DSL.field("m.sender_id").eq(DSL.field("u.id")))
                .where(JooqUtils.uuidEq("m.team_id", teamId))
                .orderBy(DSL.field("m.created_at").desc())
                .limit(fetchSize)
                .offset(pageable.getOffset())
                .fetch(r -> new ChatMessageResponse(
                        UUID.fromString(r.get("messageId", String.class)),
                        UUID.fromString(r.get("teamId", String.class)),
                        UUID.fromString(r.get("senderId", String.class)),
                        r.get("senderNickname", String.class),
                        r.get("content", String.class),
                        r.get("createdAt", LocalDateTime.class)
                ));

        boolean hasNext = result.size() == fetchSize;
        List<ChatMessageResponse> content = hasNext ? result.subList(0, pageable.getPageSize()) : result;

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
