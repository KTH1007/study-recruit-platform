package com.study.platform.domain.team.application

import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.domain.team.usecase.DelegateLeaderUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DelegateLeaderService(
    private val teamMemberRepository: TeamMemberRepository
) : DelegateLeaderUseCase {

    @Transactional
    override fun execute(userId: UUID, teamId: UUID, targetUserId: UUID) {
        if (userId == targetUserId) throw CustomException(ErrorCode.CANNOT_DELEGATE_TO_SELF)

        val currentLeader = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
            ?: throw CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND)
        currentLeader.validateIsLeader()

        val newLeader = teamMemberRepository.findByTeamIdAndUserId(teamId, targetUserId)
            ?: throw CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND)

        currentLeader.downgradeToMember()
        newLeader.upgradeToLeader()
    }
}
