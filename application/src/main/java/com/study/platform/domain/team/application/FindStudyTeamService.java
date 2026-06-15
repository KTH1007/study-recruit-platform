package com.study.platform.domain.team.application;

import com.study.platform.domain.team.dto.response.StudyTeamResponse;
import com.study.platform.domain.team.model.StudyTeam;
import com.study.platform.domain.team.model.StudyTeamRepository;
import com.study.platform.domain.team.usecase.FindStudyTeamUseCase;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindStudyTeamService implements FindStudyTeamUseCase {

    private final StudyTeamRepository studyTeamRepository;

    @Override
    public StudyTeamResponse execute(UUID teamId) {
        StudyTeam team = studyTeamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
        return StudyTeamResponse.from(team);
    }

    @Override
    public StudyTeamResponse executeByPostId(UUID postId) {
        StudyTeam team = studyTeamRepository.findByPostId(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
        return StudyTeamResponse.from(team);
    }
}
