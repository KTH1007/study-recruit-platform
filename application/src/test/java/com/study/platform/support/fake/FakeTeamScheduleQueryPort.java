package com.study.platform.support.fake;

import com.study.platform.domain.team.dto.response.TeamScheduleResponse;
import com.study.platform.domain.team.port.TeamScheduleQueryPort;

import java.util.List;
import java.util.UUID;

public class FakeTeamScheduleQueryPort implements TeamScheduleQueryPort {

    private final FakeTeamScheduleRepository repository;

    public FakeTeamScheduleQueryPort(FakeTeamScheduleRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TeamScheduleResponse> findAllByTeamId(UUID teamId) {
        return repository.findAllByTeamId(teamId).stream()
                .map(TeamScheduleResponse::from)
                .toList();
    }
}
