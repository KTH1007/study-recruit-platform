package com.study.platform.domain.team.application

import com.study.platform.domain.team.model.StudyTeamRepository
import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.domain.team.usecase.RemoveTeamMemberUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RemoveTeamMemberService(
    private val teamMemberRepository: TeamMemberRepository,
    private val studyTeamRepository: StudyTeamRepository
) : RemoveTeamMemberUseCase {

    @Transactional
    override fun execute(userId: UUID, teamId: UUID, targetUserId: UUID) {
        if (userId == targetUserId) throw CustomException(ErrorCode.CANNOT_REMOVE_SELF)

        studyTeamRepository.findByIdForUpdate(teamId)
            ?: throw CustomException(ErrorCode.TEAM_NOT_FOUND)

        val currentLeader = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
            ?: throw CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND)
        currentLeader.validateIsLeader()

        val target = teamMemberRepository.findByTeamIdAndUserId(teamId, targetUserId)
            ?: throw CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND)
        teamMemberRepository.delete(target)
    }
}
