package com.study.platform.domain.team.usecase

import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest
import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import java.util.UUID

interface CreateTeamScheduleUseCase {
    fun execute(userId: UUID, teamId: UUID, request: TeamScheduleCreateRequest): TeamScheduleResponse
}
