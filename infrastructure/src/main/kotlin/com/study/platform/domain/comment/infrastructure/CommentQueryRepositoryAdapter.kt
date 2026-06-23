package com.study.platform.domain.comment.infrastructure

import com.study.platform.domain.comment.dto.response.CommentResponse
import com.study.platform.domain.comment.port.CommentQueryPort
import com.study.platform.global.jooq.JooqUtils.binToUuid
import com.study.platform.global.jooq.JooqUtils.uuidEq
import com.study.platform.jooq.tables.references.COMMENTS
import com.study.platform.jooq.tables.references.USERS
import org.jooq.DSLContext
import org.jooq.SortField
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
        val c = COMMENTS.`as`("c")
        val u = USERS.`as`("u")
        val orderFields = buildOrderFields(c, pageable)

        val content = dsl.select(
            binToUuid(c.ID).`as`("id"),
            binToUuid(u.ID).`as`("authorId"),
            u.NICKNAME.`as`("authorNickname"),
            c.CONTENT.`as`("content"),
            c.CREATED_AT.`as`("createdAt")
        )
            .from(c)
            .join(u).on(c.AUTHOR_ID.eq(u.ID))
            .where(uuidEq(c.POST_ID, postId))
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
            dsl.selectOne().from(c).where(uuidEq(c.POST_ID, postId))
        )

        return PageImpl(content, pageable, total.toLong())
    }

    private fun buildOrderFields(c: com.study.platform.jooq.tables.Comments, pageable: Pageable): List<SortField<*>> {
        val orderFields = pageable.sort.map { order ->
            if (order.isAscending) c.CREATED_AT.asc() else c.CREATED_AT.desc()
        }.toMutableList<SortField<*>>()
        if (orderFields.isEmpty()) orderFields.add(c.CREATED_AT.asc())
        return orderFields
    }
}
