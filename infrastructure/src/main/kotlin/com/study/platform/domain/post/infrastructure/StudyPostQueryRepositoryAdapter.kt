package com.study.platform.domain.post.infrastructure

import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse
import com.study.platform.domain.post.model.StudyPostStatus
import com.study.platform.domain.post.port.StudyPostQueryPort
import com.study.platform.global.jooq.JooqUtils.binToUuid
import com.study.platform.jooq.tables.references.STUDY_POSTS
import com.study.platform.jooq.tables.references.USERS
import org.jooq.Condition
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
class StudyPostQueryRepositoryAdapter(
    private val dsl: DSLContext
) : StudyPostQueryPort {

    override fun findAllWithFilter(techStack: String?, status: StudyPostStatus?, pageable: Pageable): Page<StudyPostSummaryResponse> {
        val p = STUDY_POSTS.`as`("p")
        val u = USERS.`as`("u")
        val condition = buildCondition(p, techStack, status)
        val orderFields = buildOrderFields(p, pageable)

        val content = dsl.select(
            binToUuid(p.ID).`as`("id"),
            u.NICKNAME.`as`("authorNickname"),
            p.TITLE.`as`("title"),
            p.TECH_STACK.`as`("techStack"),
            p.MAX_MEMBERS.`as`("maxMembers"),
            p.DEADLINE.`as`("deadline"),
            p.STATUS.`as`("status"),
            p.CREATED_AT.`as`("createdAt")
        )
            .from(p)
            .join(u).on(p.AUTHOR_ID.eq(u.ID))
            .where(condition)
            .orderBy(orderFields)
            .limit(pageable.pageSize)
            .offset(pageable.offset)
            .fetch { r ->
                StudyPostSummaryResponse(
                    UUID.fromString(r.get("id", String::class.java)),
                    r.get("authorNickname", String::class.java),
                    r.get("title", String::class.java),
                    r.get("techStack", String::class.java),
                    r.get("maxMembers", Int::class.java),
                    r.get("deadline", LocalDateTime::class.java),
                    StudyPostStatus.valueOf(r.get("status", String::class.java)),
                    r.get("createdAt", LocalDateTime::class.java)
                )
            }

        val total = dsl.fetchCount(
            dsl.selectOne().from(p).where(condition)
        )

        return PageImpl(content, pageable, total.toLong())
    }

    private fun buildCondition(p: com.study.platform.jooq.tables.StudyPosts, techStack: String?, status: StudyPostStatus?): Condition {
        var condition: Condition = DSL.trueCondition()
        if (!techStack.isNullOrBlank()) condition = condition.and(p.TECH_STACK.like("%$techStack%"))
        if (status != null) condition = condition.and(p.STATUS.cast(String::class.java).eq(status.name))
        return condition
    }

    private fun buildOrderFields(p: com.study.platform.jooq.tables.StudyPosts, pageable: Pageable): List<SortField<*>> {
        val orderFields = pageable.sort.map { order ->
            when (order.property) {
                "deadline" -> if (order.isAscending) p.DEADLINE.asc() else p.DEADLINE.desc()
                "status" -> if (order.isAscending) p.STATUS.asc() else p.STATUS.desc()
                else -> if (order.isAscending) p.CREATED_AT.asc() else p.CREATED_AT.desc()
            }
        }.toMutableList<SortField<*>>()
        if (orderFields.isEmpty()) orderFields.add(p.CREATED_AT.desc())
        return orderFields
    }
}
