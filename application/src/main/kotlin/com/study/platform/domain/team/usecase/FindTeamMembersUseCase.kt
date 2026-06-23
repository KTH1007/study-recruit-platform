package com.study.platform.domain.team.usecase

import com.study.platform.domain.team.dto.response.TeamMemberResponse
import java.util.UUID

interface FindTeamMembersUseCase {
    fun execute(teamId: UUID): List<TeamMemberResponse>
}
