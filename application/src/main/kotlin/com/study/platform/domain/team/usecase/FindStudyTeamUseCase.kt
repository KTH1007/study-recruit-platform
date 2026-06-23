package com.study.platform.domain.team.usecase

import com.study.platform.domain.team.dto.response.StudyTeamResponse
import java.util.UUID

interface FindStudyTeamUseCase {
    fun execute(teamId: UUID): StudyTeamResponse
    fun executeByPostId(postId: UUID): StudyTeamResponse
}
