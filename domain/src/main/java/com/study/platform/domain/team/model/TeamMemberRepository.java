package com.study.platform.domain.team.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamMemberRepository {

    TeamMember save(TeamMember member);
    void delete(TeamMember member);
    List<TeamMember> findAllByTeamId(UUID teamId);
    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);
    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);
    long countByTeamId(UUID teamId);
    void deleteAllByTeamId(UUID teamId);
}
