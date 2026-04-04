package com.study.platform.domain.team.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudyTeamRepository extends JpaRepository<StudyTeam, UUID> {

    Optional<StudyTeam> findByPostId(UUID postId);
}
