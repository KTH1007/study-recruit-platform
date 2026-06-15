package com.study.platform.domain.team.application;

import com.study.platform.domain.team.dto.response.TeamMemberResponse;
import com.study.platform.domain.team.port.TeamMemberQueryPort;
import com.study.platform.domain.team.usecase.FindTeamMembersUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindTeamMembersService implements FindTeamMembersUseCase {

    private final TeamMemberQueryPort teamMemberQueryPort;

    @Override
    public List<TeamMemberResponse> execute(UUID teamId) {
        return teamMemberQueryPort.findAllByTeamId(teamId);
    }
}
