package com.study.platform.domain.team.usecase;

import com.study.platform.domain.team.dto.response.TeamMemberResponse;

import java.util.List;
import java.util.UUID;

public interface FindTeamMembersUseCase {
    List<TeamMemberResponse> execute(UUID teamId);
}
