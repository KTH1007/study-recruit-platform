package com.study.platform.domain.apply.infrastructure

import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.domain.apply.model.ApplyStatus
import com.study.platform.domain.apply.port.ApplyQueryPort
import com.study.platform.global.jooq.JooqUtils
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class ApplyQueryRepositoryAdapter(
    private val dsl: DSLContext
) : ApplyQueryPort {

    override fun findAllByPostId(postId: UUID): List<ApplyResponse> =
        dsl.select(
            DSL.field("BIN_TO_UUID(a.id)").`as`("id"),
            DSL.field("u.nickname").`as`("applicantNickname"),
            DSL.field("u.tech_stack").`as`("applicantTechStack"),
            DSL.field("a.message").`as`("message"),
            DSL.field("a.status").`as`("status"),
            DSL.field("a.created_at").`as`("createdAt")
        )
        .from(DSL.table("applies").`as`("a"))
        .join(DSL.table("users").`as`("u"))
        .on(DSL.field("a.applicant_id").eq(DSL.field("u.id")))
        .where(JooqUtils.uuidEq("a.post_id", postId))
        .fetch { r ->
            ApplyResponse(
                id = UUID.fromString(r.get("id", String::class.java)),
                applicantNickname = r.get("applicantNickname", String::class.java),
                applicantTechStack = r.get("applicantTechStack", String::class.java),
                message = r.get("message", String::class.java),
                status = ApplyStatus.valueOf(r.get("status", String::class.java)),
                createdAt = r.get("createdAt", LocalDateTime::class.java)
            )
        }
}
