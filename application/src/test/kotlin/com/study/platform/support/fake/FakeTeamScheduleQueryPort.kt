package com.study.platform.support.fake

import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import com.study.platform.domain.team.port.TeamScheduleQueryPort
import java.util.UUID

class FakeTeamScheduleQueryPort(
    private val repository: FakeTeamScheduleRepository
) : TeamScheduleQueryPort {

    override fun findAllByTeamId(teamId: UUID): List<TeamScheduleResponse> =
        repository.findAllByTeamId(teamId).map { TeamScheduleResponse.from(it) }
}
