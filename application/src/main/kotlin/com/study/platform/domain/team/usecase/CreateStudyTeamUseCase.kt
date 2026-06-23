package com.study.platform.domain.team.usecase

import com.study.platform.domain.apply.event.ApplyApprovedEvent

interface CreateStudyTeamUseCase {
    fun execute(event: ApplyApprovedEvent)
}
