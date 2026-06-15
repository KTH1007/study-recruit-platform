package com.study.platform.support.fake;

import com.study.platform.domain.team.model.StudyTeam;
import com.study.platform.domain.team.model.StudyTeamRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeStudyTeamRepository implements StudyTeamRepository {

    private final Map<UUID, StudyTeam> store = new HashMap<>();

    @Override
    public StudyTeam save(StudyTeam team) {
        if (team.getId() == null) {
            ReflectionTestUtils.setField(team, "id", UUID.randomUUID());
        }
        store.put(team.getId(), team);
        return team;
    }

    @Override
    public Optional<StudyTeam> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void delete(StudyTeam team) {
        store.remove(team.getId());
    }

    @Override
    public Optional<StudyTeam> findByPostId(UUID postId) {
        return store.values().stream()
                .filter(t -> t.getPost().getId().equals(postId))
                .findFirst();
    }
}
