package com.study.platform.domain.team.application

import com.study.platform.domain.team.dto.response.TeamMemberResponse
import com.study.platform.domain.team.port.TeamMemberQueryPort
import com.study.platform.domain.team.usecase.FindTeamMembersUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FindTeamMembersService(
    private val teamMemberQueryPort: TeamMemberQueryPort
) : FindTeamMembersUseCase {

    override fun execute(teamId: UUID): List<TeamMemberResponse> {
        return teamMemberQueryPort.findAllByTeamId(teamId)
    }
}
