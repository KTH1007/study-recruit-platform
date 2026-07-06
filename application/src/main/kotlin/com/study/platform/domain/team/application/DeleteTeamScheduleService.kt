package com.study.platform.domain.team.application

import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.domain.team.model.TeamScheduleRepository
import com.study.platform.domain.team.usecase.DeleteTeamScheduleUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteTeamScheduleService(
    private val teamScheduleRepository: TeamScheduleRepository,
    private val teamMemberRepository: TeamMemberRepository
) : DeleteTeamScheduleUseCase {

    @Transactional
    override fun execute(userId: UUID, teamId: UUID, scheduleId: UUID) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw CustomException(ErrorCode.NOT_TEAM_MEMBER)
        }
        val schedule = teamScheduleRepository.findById(scheduleId)
            ?: throw CustomException(ErrorCode.TEAM_SCHEDULE_NOT_FOUND)
        schedule.validateBelongsToTeam(teamId)
        teamScheduleRepository.delete(schedule)
    }
}
