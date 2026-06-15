package com.study.platform.domain.team.usecase;

import java.util.UUID;

public interface RemoveTeamMemberUseCase {
    void execute(UUID userId, UUID teamId, UUID targetUserId);
}
