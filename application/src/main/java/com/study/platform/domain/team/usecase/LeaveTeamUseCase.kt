package com.study.platform.domain.team.usecase

import java.util.UUID

interface LeaveTeamUseCase {
    fun execute(userId: UUID, teamId: UUID)
}
