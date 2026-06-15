package com.study.platform.domain.team.infrastructure;

import com.study.platform.domain.team.model.TeamMember;
import com.study.platform.domain.team.model.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TeamMemberRepositoryAdapter implements TeamMemberRepository {

    private final TeamMemberJpaRepository teamMemberJpaRepository;

    @Override
    public TeamMember save(TeamMember member) {
        return teamMemberJpaRepository.save(member);
    }

    @Override
    public void delete(TeamMember member) {
        teamMemberJpaRepository.delete(member);
    }

    @Override
    public List<TeamMember> findAllByTeamId(UUID teamId) {
        return teamMemberJpaRepository.findAllByTeamId(teamId);
    }

    @Override
    public Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId) {
        return teamMemberJpaRepository.findByTeamIdAndUserId(teamId, userId);
    }

    @Override
    public boolean existsByTeamIdAndUserId(UUID teamId, UUID userId) {
        return teamMemberJpaRepository.existsByTeamIdAndUserId(teamId, userId);
    }

    @Override
    public long countByTeamId(UUID teamId) {
        return teamMemberJpaRepository.countByTeamId(teamId);
    }

    @Override
    public void deleteAllByTeamId(UUID teamId) {
        teamMemberJpaRepository.deleteAllByTeamId(teamId);
    }
}
