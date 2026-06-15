package com.study.platform.domain.team.usecase;

import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest;
import com.study.platform.domain.team.dto.response.TeamScheduleResponse;

import java.util.UUID;

public interface CreateTeamScheduleUseCase {
    TeamScheduleResponse execute(UUID userId, UUID teamId, TeamScheduleCreateRequest request);
}
