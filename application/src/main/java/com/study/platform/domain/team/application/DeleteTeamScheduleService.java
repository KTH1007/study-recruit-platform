package com.study.platform.domain.team.application;

import com.study.platform.domain.team.model.TeamMemberRepository;
import com.study.platform.domain.team.model.TeamSchedule;
import com.study.platform.domain.team.model.TeamScheduleRepository;
import com.study.platform.domain.team.usecase.DeleteTeamScheduleUseCase;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteTeamScheduleService implements DeleteTeamScheduleUseCase {

    private final TeamScheduleRepository teamScheduleRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    @Transactional
    public void execute(UUID userId, UUID teamId, UUID scheduleId) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_MEMBER);
        }
        TeamSchedule schedule = teamScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_SCHEDULE_NOT_FOUND));
        teamScheduleRepository.delete(schedule);
    }
}
