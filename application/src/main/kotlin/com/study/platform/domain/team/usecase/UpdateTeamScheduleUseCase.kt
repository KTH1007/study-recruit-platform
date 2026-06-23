package com.study.platform.domain.team.usecase

import com.study.platform.domain.team.dto.request.TeamScheduleUpdateRequest
import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import java.util.UUID

interface UpdateTeamScheduleUseCase {
    fun execute(userId: UUID, teamId: UUID, scheduleId: UUID, request: TeamScheduleUpdateRequest): TeamScheduleResponse
}
