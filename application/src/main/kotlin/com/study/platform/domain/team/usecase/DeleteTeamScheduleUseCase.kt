package com.study.platform.domain.team.usecase

import java.util.UUID

interface DeleteTeamScheduleUseCase {
    fun execute(userId: UUID, teamId: UUID, scheduleId: UUID)
}
