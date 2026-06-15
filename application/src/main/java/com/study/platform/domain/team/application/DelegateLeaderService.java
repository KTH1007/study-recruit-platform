package com.study.platform.domain.team.application;

import com.study.platform.domain.team.model.TeamMember;
import com.study.platform.domain.team.model.TeamMemberRepository;
import com.study.platform.domain.team.usecase.DelegateLeaderUseCase;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DelegateLeaderService implements DelegateLeaderUseCase {

    private final TeamMemberRepository teamMemberRepository;

    @Override
    @Transactional
    public void execute(UUID userId, UUID teamId, UUID targetUserId) {
        TeamMember currentLeader = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND));
        currentLeader.validateIsLeader();

        TeamMember newLeader = teamMemberRepository.findByTeamIdAndUserId(teamId, targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        currentLeader.downgradeToMember();
        newLeader.upgradeToLeader();
    }
}
