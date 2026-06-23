package com.study.platform.support.fake

import com.study.platform.domain.team.dto.response.TeamMemberResponse
import com.study.platform.domain.team.port.TeamMemberQueryPort
import java.util.UUID

class FakeTeamMemberQueryPort(
    private val repository: FakeTeamMemberRepository
) : TeamMemberQueryPort {

    override fun findAllByTeamId(teamId: UUID): List<TeamMemberResponse> =
        repository.findAllByTeamId(teamId).map { TeamMemberResponse.from(it) }
}
