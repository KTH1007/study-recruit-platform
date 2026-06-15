package com.study.platform.domain.team.model;

import java.util.Optional;
import java.util.UUID;

public interface StudyTeamRepository {

    StudyTeam save(StudyTeam team);
    Optional<StudyTeam> findById(UUID id);
    void delete(StudyTeam team);
    Optional<StudyTeam> findByPostId(UUID postId);
}
