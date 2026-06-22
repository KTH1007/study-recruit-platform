package com.study.platform.domain.team.infrastructure

import com.study.platform.domain.team.dto.response.TeamMemberResponse
import com.study.platform.domain.team.model.TeamMemberRole
import com.study.platform.domain.team.port.TeamMemberQueryPort
import com.study.platform.global.jooq.JooqUtils
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class TeamMemberQueryRepositoryAdapter(
    private val dsl: DSLContext
) : TeamMemberQueryPort {

    override fun findAllByTeamId(teamId: UUID): List<TeamMemberResponse> =
        dsl
            .select(
                DSL.field("BIN_TO_UUID(m.id)").`as`("id"),
                DSL.field("BIN_TO_UUID(u.id)").`as`("userId"),
                DSL.field("u.nickname").`as`("nickname"),
                DSL.field("m.role").`as`("role")
            )
            .from(DSL.table("team_members").`as`("m"))
            .join(DSL.table("users").`as`("u"))
            .on(DSL.field("m.user_id").eq(DSL.field("u.id")))
            .where(JooqUtils.uuidEq("m.team_id", teamId))
            .fetch { r ->
                TeamMemberResponse(
                    UUID.fromString(r.get("id", String::class.java)),
                    UUID.fromString(r.get("userId", String::class.java)),
                    r.get("nickname", String::class.java),
                    TeamMemberRole.valueOf(r.get("role", String::class.java))
                )
            }
}
