package com.study.platform.domain.team.usecase

import java.util.UUID

interface DelegateLeaderUseCase {
    fun execute(userId: UUID, teamId: UUID, targetUserId: UUID)
}
