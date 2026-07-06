package com.study.platform.domain.team.application

import com.study.platform.domain.team.dto.request.TeamScheduleUpdateRequest
import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.domain.team.model.TeamScheduleRepository
import com.study.platform.domain.team.usecase.UpdateTeamScheduleUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UpdateTeamScheduleService(
    private val teamScheduleRepository: TeamScheduleRepository,
    private val teamMemberRepository: TeamMemberRepository
) : UpdateTeamScheduleUseCase {

    @Transactional
    override fun execute(userId: UUID, teamId: UUID, scheduleId: UUID, request: TeamScheduleUpdateRequest): TeamScheduleResponse {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw CustomException(ErrorCode.NOT_TEAM_MEMBER)
        }
        val schedule = teamScheduleRepository.findById(scheduleId)
            ?: throw CustomException(ErrorCode.TEAM_SCHEDULE_NOT_FOUND)
        schedule.validateBelongsToTeam(teamId)
        schedule.update(request.title, request.description, request.scheduledAt!!)
        return TeamScheduleResponse.from(schedule)
    }
}
