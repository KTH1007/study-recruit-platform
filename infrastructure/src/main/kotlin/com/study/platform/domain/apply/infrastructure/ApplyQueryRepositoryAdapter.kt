package com.study.platform.domain.apply.infrastructure

import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.domain.apply.model.ApplyStatus
import com.study.platform.domain.apply.port.ApplyQueryPort
import com.study.platform.global.jooq.JooqUtils.binToUuid
import com.study.platform.global.jooq.JooqUtils.uuidEq
import com.study.platform.jooq.tables.references.APPLIES
import com.study.platform.jooq.tables.references.USERS
import org.jooq.DSLContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class ApplyQueryRepositoryAdapter(
    private val dsl: DSLContext
) : ApplyQueryPort {

    override fun findAllByPostId(postId: UUID, pageable: Pageable): Page<ApplyResponse> {
        val a = APPLIES.`as`("a")
        val u = USERS.`as`("u")

        val content = dsl.select(
            binToUuid(a.ID).`as`("id"),
            u.NICKNAME.`as`("applicantNickname"),
            u.TECH_STACK.`as`("applicantTechStack"),
            a.MESSAGE.`as`("message"),
            a.STATUS.`as`("status"),
            a.CREATED_AT.`as`("createdAt")
        )
            .from(a)
            .join(u).on(a.APPLICANT_ID.eq(u.ID))
            .where(uuidEq(a.POST_ID, postId))
            .orderBy(a.CREATED_AT.desc())
            .limit(pageable.pageSize)
            .offset(pageable.offset)
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

        val total = dsl.selectCount()
            .from(a)
            .where(uuidEq(a.POST_ID, postId))
            .fetchOne(0, Long::class.java) ?: 0L

        return PageImpl(content, pageable, total)
    }
}
