package com.study.platform.domain.team.infrastructure

import com.study.platform.domain.team.dto.response.TeamMemberResponse
import com.study.platform.domain.team.model.TeamMemberRole
import com.study.platform.domain.team.port.TeamMemberQueryPort
import com.study.platform.global.jooq.JooqUtils.binToUuid
import com.study.platform.global.jooq.JooqUtils.uuidEq
import com.study.platform.jooq.tables.references.TEAM_MEMBERS
import com.study.platform.jooq.tables.references.USERS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class TeamMemberQueryRepositoryAdapter(
    private val dsl: DSLContext
) : TeamMemberQueryPort {

    override fun findAllByTeamId(teamId: UUID): List<TeamMemberResponse> {
        val m = TEAM_MEMBERS.`as`("m")
        val u = USERS.`as`("u")

        return dsl.select(
            binToUuid(m.ID).`as`("id"),
            binToUuid(u.ID).`as`("userId"),
            u.NICKNAME.`as`("nickname"),
            m.ROLE.`as`("role")
        )
            .from(m)
            .join(u).on(m.USER_ID.eq(u.ID))
            .where(uuidEq(m.TEAM_ID, teamId))
            .fetch { r ->
                TeamMemberResponse(
                    UUID.fromString(r.get("id", String::class.java)),
                    UUID.fromString(r.get("userId", String::class.java)),
                    r.get("nickname", String::class.java),
                    TeamMemberRole.valueOf(r.get("role", String::class.java))
                )
            }
    }
}
