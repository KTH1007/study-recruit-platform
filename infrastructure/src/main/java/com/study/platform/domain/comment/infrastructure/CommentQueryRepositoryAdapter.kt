package com.study.platform.domain.comment.infrastructure

import com.study.platform.domain.comment.dto.response.CommentResponse
import com.study.platform.domain.comment.port.CommentQueryPort
import com.study.platform.global.jooq.JooqUtils
import org.jooq.DSLContext
import org.jooq.SortField
import org.jooq.impl.DSL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class CommentQueryRepositoryAdapter(
    private val dsl: DSLContext
) : CommentQueryPort {

    override fun findAllByPostId(postId: UUID, pageable: Pageable): Page<CommentResponse> {
        val orderFields = buildOrderFields(pageable)

        val content = dsl
            .select(
                DSL.field("BIN_TO_UUID(c.id)").`as`("id"),
                DSL.field("BIN_TO_UUID(u.id)").`as`("authorId"),
                DSL.field("u.nickname").`as`("authorNickname"),
                DSL.field("c.content").`as`("content"),
                DSL.field("c.created_at").`as`("createdAt")
            )
            .from(DSL.table("comments").`as`("c"))
            .join(DSL.table("users").`as`("u"))
            .on(DSL.field("c.author_id").eq(DSL.field("u.id")))
            .where(JooqUtils.uuidEq("c.post_id", postId))
            .orderBy(orderFields)
            .limit(pageable.pageSize)
            .offset(pageable.offset)
            .fetch { r ->
                CommentResponse(
                    UUID.fromString(r.get("id", String::class.java)),
                    UUID.fromString(r.get("authorId", String::class.java)),
                    r.get("authorNickname", String::class.java),
                    r.get("content", String::class.java),
                    r.get("createdAt", LocalDateTime::class.java)
                )
            }

        val total = dsl.fetchCount(
            dsl.selectOne()
                .from(DSL.table("comments").`as`("c"))
                .where(JooqUtils.uuidEq("c.post_id", postId))
        )

        return PageImpl(content, pageable, total.toLong())
    }

    private fun buildOrderFields(pageable: Pageable): List<SortField<*>> {
        val orderFields = mutableListOf<SortField<*>>()
        pageable.sort.forEach { order ->
            val column = "c.created_at"
            orderFields.add(if (order.isAscending) DSL.field(column).asc() else DSL.field(column).desc())
        }
        if (orderFields.isEmpty()) {
            orderFields.add(DSL.field("c.created_at").asc())
        }
        return orderFields
    }
}
