package com.study.platform.domain.team.usecase;

import com.study.platform.domain.apply.event.ApplyApprovedEvent;

public interface CreateStudyTeamUseCase {
    void execute(ApplyApprovedEvent event);
}
