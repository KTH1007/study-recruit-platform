package com.study.platform.domain.team.port

import com.study.platform.domain.team.dto.response.TeamMemberResponse
import java.util.UUID

interface TeamMemberQueryPort {
    fun findAllByTeamId(teamId: UUID): List<TeamMemberResponse>
}
