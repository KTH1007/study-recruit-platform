package com.study.platform.domain.team.usecase

import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import java.util.UUID

interface FindTeamSchedulesUseCase {
    fun execute(userId: UUID, teamId: UUID): List<TeamScheduleResponse>
}
