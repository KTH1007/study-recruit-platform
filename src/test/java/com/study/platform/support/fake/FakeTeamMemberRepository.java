package com.study.platform.support.fake;

import com.study.platform.domain.team.model.TeamMember;
import com.study.platform.domain.team.model.TeamMemberRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeTeamMemberRepository implements TeamMemberRepository {

    private final Map<UUID, TeamMember> store = new HashMap<>();

    @Override
    public TeamMember save(TeamMember member) {
        if (member.getId() == null) {
            ReflectionTestUtils.setField(member, "id", UUID.randomUUID());
        }
        store.put(member.getId(), member);
        return member;
    }

    @Override
    public void delete(TeamMember member) {
        store.remove(member.getId());
    }

    @Override
    public List<TeamMember> findAllByTeamId(UUID teamId) {
        return store.values().stream()
                .filter(m -> m.getTeam().getId().equals(teamId))
                .toList();
    }

    @Override
    public Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId) {
        return store.values().stream()
                .filter(m -> m.getTeam().getId().equals(teamId)
                        && m.getUser().getId().equals(userId))
                .findFirst();
    }

    @Override
    public boolean existsByTeamIdAndUserId(UUID teamId, UUID userId) {
        return findByTeamIdAndUserId(teamId, userId).isPresent();
    }

    @Override
    public long countByTeamId(UUID teamId) {
        return store.values().stream()
                .filter(m -> m.getTeam().getId().equals(teamId))
                .count();
    }

    @Override
    public void deleteAllByTeamId(UUID teamId) {
        List<UUID> toRemove = store.values().stream()
                .filter(m -> m.getTeam().getId().equals(teamId))
                .map(TeamMember::getId)
                .toList();
        toRemove.forEach(store::remove);
    }
}
