package com.study.platform.domain.team.port

import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import java.util.UUID

interface TeamScheduleQueryPort {
    fun findAllByTeamId(teamId: UUID): List<TeamScheduleResponse>
}
