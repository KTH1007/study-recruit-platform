package com.study.platform.domain.team.infrastructure;

import com.study.platform.domain.team.model.StudyTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudyTeamJpaRepository extends JpaRepository<StudyTeam, UUID> {

    Optional<StudyTeam> findByPostId(UUID postId);
}
