package com.study.platform.domain.team.port;

import com.study.platform.domain.team.dto.response.TeamMemberResponse;

import java.util.List;
import java.util.UUID;

public interface TeamMemberQueryPort {

    List<TeamMemberResponse> findAllByTeamId(UUID teamId);
}
