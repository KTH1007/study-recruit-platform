package com.study.platform.domain.team.usecase

import java.util.UUID

interface RemoveTeamMemberUseCase {
    fun execute(userId: UUID, teamId: UUID, targetUserId: UUID)
}
