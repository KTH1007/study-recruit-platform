package com.study.platform.domain.post.infrastructure

import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse
import com.study.platform.domain.post.model.StudyPostStatus
import com.study.platform.domain.post.port.StudyPostQueryPort
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
        val condition = buildCondition(techStack, status)
        val orderFields = buildOrderFields(pageable)

        val content = dsl
            .select(
                DSL.field("BIN_TO_UUID(p.id)").`as`("id"),
                DSL.field("u.nickname").`as`("authorNickname"),
                DSL.field("p.title").`as`("title"),
                DSL.field("p.tech_stack").`as`("techStack"),
                DSL.field("p.max_members").`as`("maxMembers"),
                DSL.field("p.deadline").`as`("deadline"),
                DSL.field("p.status").`as`("status"),
                DSL.field("p.created_at").`as`("createdAt")
            )
            .from(DSL.table("study_posts").`as`("p"))
            .join(DSL.table("users").`as`("u"))
            .on(DSL.field("p.author_id").eq(DSL.field("u.id")))
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
            dsl.selectOne()
                .from(DSL.table("study_posts").`as`("p"))
                .where(condition)
        )

        return PageImpl(content, pageable, total.toLong())
    }

    private fun buildCondition(techStack: String?, status: StudyPostStatus?): Condition {
        var condition: Condition = DSL.trueCondition()
        if (!techStack.isNullOrBlank()) {
            condition = condition.and(DSL.field("p.tech_stack").like("%$techStack%"))
        }
        if (status != null) {
            condition = condition.and(DSL.field("p.status").eq(status.name))
        }
        return condition
    }

    private fun buildOrderFields(pageable: Pageable): List<SortField<*>> {
        val orderFields = mutableListOf<SortField<*>>()
        pageable.sort.forEach { order ->
            val column = toColumnName(order.property)
            orderFields.add(if (order.isAscending) DSL.field(column).asc() else DSL.field(column).desc())
        }
        if (orderFields.isEmpty()) {
            orderFields.add(DSL.field("p.created_at").desc())
        }
        return orderFields
    }

    private fun toColumnName(property: String): String = when (property) {
        "createdAt" -> "p.created_at"
        "deadline" -> "p.deadline"
        "status" -> "p.status"
        else -> "p.created_at"
    }
}
