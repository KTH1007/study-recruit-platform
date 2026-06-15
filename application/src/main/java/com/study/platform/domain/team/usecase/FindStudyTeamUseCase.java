package com.study.platform.domain.team.usecase;

import com.study.platform.domain.team.dto.response.StudyTeamResponse;

import java.util.UUID;

public interface FindStudyTeamUseCase {
    StudyTeamResponse execute(UUID teamId);
    StudyTeamResponse executeByPostId(UUID postId);
}
