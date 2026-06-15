package com.study.platform.domain.team.usecase;

import com.study.platform.domain.team.dto.response.TeamScheduleResponse;

import java.util.List;
import java.util.UUID;

public interface FindTeamSchedulesUseCase {
    List<TeamScheduleResponse> execute(UUID userId, UUID teamId);
}
