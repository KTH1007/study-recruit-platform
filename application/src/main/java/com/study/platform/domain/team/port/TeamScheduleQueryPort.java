package com.study.platform.domain.team.port;

import com.study.platform.domain.team.dto.response.TeamScheduleResponse;

import java.util.List;
import java.util.UUID;

public interface TeamScheduleQueryPort {

    List<TeamScheduleResponse> findAllByTeamId(UUID teamId);
}
