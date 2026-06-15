package com.study.platform.domain.team.usecase;

import com.study.platform.domain.team.dto.request.TeamScheduleUpdateRequest;
import com.study.platform.domain.team.dto.response.TeamScheduleResponse;

import java.util.UUID;

public interface UpdateTeamScheduleUseCase {
    TeamScheduleResponse execute(UUID userId, UUID teamId, UUID scheduleId, TeamScheduleUpdateRequest request);
}
