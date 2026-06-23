package com.study.platform.domain.team.application

import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.domain.team.port.TeamScheduleQueryPort
import com.study.platform.domain.team.usecase.FindTeamSchedulesUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FindTeamSchedulesService(
    private val teamScheduleQueryPort: TeamScheduleQueryPort,
    private val teamMemberRepository: TeamMemberRepository
) : FindTeamSchedulesUseCase {

    override fun execute(userId: UUID, teamId: UUID): List<TeamScheduleResponse> {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw CustomException(ErrorCode.NOT_TEAM_MEMBER)
        }
        return teamScheduleQueryPort.findAllByTeamId(teamId)
    }
}
