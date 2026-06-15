package com.study.platform.domain.team.usecase;

import java.util.UUID;

public interface LeaveTeamUseCase {
    void execute(UUID userId, UUID teamId);
}
