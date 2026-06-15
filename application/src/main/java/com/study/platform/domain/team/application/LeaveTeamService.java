package com.study.platform.domain.team.application;

import com.study.platform.domain.team.model.StudyTeamRepository;
import com.study.platform.domain.team.model.TeamMember;
import com.study.platform.domain.team.model.TeamMemberRepository;
import com.study.platform.domain.team.usecase.LeaveTeamUseCase;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveTeamService implements LeaveTeamUseCase {

    private final StudyTeamRepository studyTeamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    @Transactional
    public void execute(UUID userId, UUID teamId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        if (member.isLeader()) {
            boolean isLastMember = teamMemberRepository.countByTeamId(teamId) == 1;
            if (isLastMember) {
                teamMemberRepository.deleteAllByTeamId(teamId);
                studyTeamRepository.delete(member.getTeam());
                return;
            }
            throw new CustomException(ErrorCode.LEADER_MUST_DELEGATE);
        }
        teamMemberRepository.delete(member);
    }
}
