package com.study.platform.domain.team.application

import com.study.platform.domain.chat.model.ChatMessageRepository
import com.study.platform.domain.team.model.StudyTeamRepository
import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.domain.team.model.TeamScheduleRepository
import com.study.platform.domain.team.usecase.LeaveTeamUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class LeaveTeamService(
    private val studyTeamRepository: StudyTeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val teamScheduleRepository: TeamScheduleRepository,
    private val chatMessageRepository: ChatMessageRepository
) : LeaveTeamUseCase {

    @Transactional
    override fun execute(userId: UUID, teamId: UUID) {
        val member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
            ?: throw CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND)

        if (member.isLeader()) {
            val isLastMember = teamMemberRepository.countByTeamId(teamId) == 1L
            if (isLastMember) {
                teamScheduleRepository.deleteAllByTeamId(teamId)
                chatMessageRepository.deleteAllByTeamId(teamId)
                teamMemberRepository.deleteAllByTeamId(teamId)
                studyTeamRepository.delete(member.team!!)
                return
            }
            throw CustomException(ErrorCode.LEADER_MUST_DELEGATE)
        }
        teamMemberRepository.delete(member)
    }
}
