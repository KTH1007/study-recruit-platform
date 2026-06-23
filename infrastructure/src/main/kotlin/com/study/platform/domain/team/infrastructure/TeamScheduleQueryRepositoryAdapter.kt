package com.study.platform.domain.team.infrastructure

import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import com.study.platform.domain.team.port.TeamScheduleQueryPort
import com.study.platform.global.jooq.JooqUtils.binToUuid
import com.study.platform.global.jooq.JooqUtils.uuidEq
import com.study.platform.jooq.tables.references.TEAM_SCHEDULES
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class TeamScheduleQueryRepositoryAdapter(
    private val dsl: DSLContext
) : TeamScheduleQueryPort {

    override fun findAllByTeamId(teamId: UUID): List<TeamScheduleResponse> {
        val s = TEAM_SCHEDULES.`as`("s")

        return dsl.select(
            binToUuid(s.ID).`as`("id"),
            binToUuid(s.TEAM_ID).`as`("teamId"),
            s.TITLE.`as`("title"),
            s.DESCRIPTION.`as`("description"),
            s.SCHEDULED_AT.`as`("scheduledAt"),
            s.CREATED_AT.`as`("createdAt")
        )
            .from(s)
            .where(uuidEq(s.TEAM_ID, teamId))
            .orderBy(s.SCHEDULED_AT.asc())
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
}
