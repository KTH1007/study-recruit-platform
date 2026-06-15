package com.study.platform.support.fake;

import com.study.platform.domain.team.dto.response.TeamMemberResponse;
import com.study.platform.domain.team.port.TeamMemberQueryPort;

import java.util.List;
import java.util.UUID;

public class FakeTeamMemberQueryPort implements TeamMemberQueryPort {

    private final FakeTeamMemberRepository repository;

    public FakeTeamMemberQueryPort(FakeTeamMemberRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TeamMemberResponse> findAllByTeamId(UUID teamId) {
        return repository.findAllByTeamId(teamId).stream()
                .map(TeamMemberResponse::from)
                .toList();
    }
}
