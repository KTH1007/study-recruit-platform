package com.study.platform.domain.team.application

import com.study.platform.domain.team.dto.response.TeamMemberResponse
import com.study.platform.domain.team.model.TeamMemberRepository
import com.study.platform.domain.team.port.TeamMemberQueryPort
import com.study.platform.domain.team.usecase.FindTeamMembersUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FindTeamMembersService(
    private val teamMemberQueryPort: TeamMemberQueryPort,
    private val teamMemberRepository: TeamMemberRepository
) : FindTeamMembersUseCase {

    override fun execute(userId: UUID, teamId: UUID): List<TeamMemberResponse> {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw CustomException(ErrorCode.NOT_TEAM_MEMBER)
        }
        return teamMemberQueryPort.findAllByTeamId(teamId)
    }
}
