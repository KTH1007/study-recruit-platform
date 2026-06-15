package com.study.platform.domain.comment.infrastructure;

import com.study.platform.domain.comment.dto.response.CommentResponse;
import com.study.platform.domain.comment.port.CommentQueryPort;
import com.study.platform.global.jooq.JooqUtils;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepositoryAdapter implements CommentQueryPort {

    private final DSLContext dsl;

    @Override
    public Page<CommentResponse> findAllByPostId(UUID postId, Pageable pageable) {
        List<SortField<?>> orderFields = buildOrderFields(pageable);

        List<CommentResponse> content = dsl
                .select(
                        DSL.field("BIN_TO_UUID(c.id)").as("id"),
                        DSL.field("BIN_TO_UUID(u.id)").as("authorId"),
                        DSL.field("u.nickname").as("authorNickname"),
                        DSL.field("c.content").as("content"),
                        DSL.field("c.created_at").as("createdAt")
                )
                .from(DSL.table("comments").as("c"))
                .join(DSL.table("users").as("u"))
                .on(DSL.field("c.author_id").eq(DSL.field("u.id")))
                .where(JooqUtils.uuidEq("c.post_id", postId))
                .orderBy(orderFields)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch(r -> new CommentResponse(
                        UUID.fromString(r.get("id", String.class)),
                        UUID.fromString(r.get("authorId", String.class)),
                        r.get("authorNickname", String.class),
                        r.get("content", String.class),
                        r.get("createdAt", LocalDateTime.class)
                ));

        int total = dsl.fetchCount(
                dsl.selectOne()
                        .from(DSL.table("comments").as("c"))
                        .where(JooqUtils.uuidEq("c.post_id", postId))
        );

        return new PageImpl<>(content, pageable, total);
    }

    private List<SortField<?>> buildOrderFields(Pageable pageable) {
        List<SortField<?>> orderFields = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            String column = "createdAt".equals(order.getProperty()) ? "c.created_at" : "c.created_at";
            orderFields.add(order.isAscending() ? DSL.field(column).asc() : DSL.field(column).desc());
        });
        if (orderFields.isEmpty()) {
            orderFields.add(DSL.field("c.created_at").asc());
        }
        return orderFields;
    }
}
