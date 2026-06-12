package com.study.platform.domain.team.infrastructure;

import com.study.platform.domain.team.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamMemberJpaRepository extends JpaRepository<TeamMember, UUID> {

    List<TeamMember> findAllByTeamId(UUID teamId);

    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);

    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    long countByTeamId(UUID teamId);

    void deleteAllByTeamId(UUID teamId);
}
