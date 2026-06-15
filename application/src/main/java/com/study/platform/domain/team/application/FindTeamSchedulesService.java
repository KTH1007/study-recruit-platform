package com.study.platform.domain.team.application;

import com.study.platform.domain.team.dto.response.TeamScheduleResponse;
import com.study.platform.domain.team.model.TeamMemberRepository;
import com.study.platform.domain.team.port.TeamScheduleQueryPort;
import com.study.platform.domain.team.usecase.FindTeamSchedulesUseCase;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindTeamSchedulesService implements FindTeamSchedulesUseCase {

    private final TeamScheduleQueryPort teamScheduleQueryPort;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public List<TeamScheduleResponse> execute(UUID userId, UUID teamId) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_MEMBER);
        }
        return teamScheduleQueryPort.findAllByTeamId(teamId);
    }
}
