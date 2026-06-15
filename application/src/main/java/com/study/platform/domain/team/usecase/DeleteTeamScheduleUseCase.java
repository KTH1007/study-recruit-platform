package com.study.platform.domain.team.usecase;

import java.util.UUID;

public interface DeleteTeamScheduleUseCase {
    void execute(UUID userId, UUID teamId, UUID scheduleId);
}
