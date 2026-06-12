package com.study.platform.domain.team.infrastructure;

import com.study.platform.domain.team.model.StudyTeam;
import com.study.platform.domain.team.model.StudyTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StudyTeamRepositoryAdapter implements StudyTeamRepository {

    private final StudyTeamJpaRepository studyTeamJpaRepository;

    @Override
    public StudyTeam save(StudyTeam team) {
        return studyTeamJpaRepository.save(team);
    }

    @Override
    public Optional<StudyTeam> findById(UUID id) {
        return studyTeamJpaRepository.findById(id);
    }

    @Override
    public void delete(StudyTeam team) {
        studyTeamJpaRepository.delete(team);
    }

    @Override
    public Optional<StudyTeam> findByPostId(UUID postId) {
        return studyTeamJpaRepository.findByPostId(postId);
    }
}
