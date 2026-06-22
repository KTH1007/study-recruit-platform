package com.study.platform.domain.team.infrastructure

import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import com.study.platform.domain.team.port.TeamScheduleQueryPort
import com.study.platform.global.jooq.JooqUtils
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class TeamScheduleQueryRepositoryAdapter(
    private val dsl: DSLContext
) : TeamScheduleQueryPort {

    override fun findAllByTeamId(teamId: UUID): List<TeamScheduleResponse> =
        dsl
            .select(
                DSL.field("BIN_TO_UUID(s.id)").`as`("id"),
                DSL.field("BIN_TO_UUID(s.team_id)").`as`("teamId"),
                DSL.field("s.title").`as`("title"),
                DSL.field("s.description").`as`("description"),
                DSL.field("s.scheduled_at").`as`("scheduledAt"),
                DSL.field("s.created_at").`as`("createdAt")
            )
            .from(DSL.table("team_schedules").`as`("s"))
            .where(JooqUtils.uuidEq("s.team_id", teamId))
            .orderBy(DSL.field("s.scheduled_at").asc())
            .fetch { r ->
                TeamScheduleResponse(
                    UUID.fromString(r.get("id", String::class.java)),
                    UUID.fromString(r.get("teamId", String::class.java)),
                    r.get("title", String::class.java),
                    r.get("description", String::class.java),
                    r.get("scheduledAt", LocalDateTime::class.java),
                    r.get("createdAt", LocalDateTime::class.java)
                )
            }
}
