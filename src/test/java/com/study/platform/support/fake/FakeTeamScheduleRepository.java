package com.study.platform.support.fake;

import com.study.platform.domain.team.model.TeamSchedule;
import com.study.platform.domain.team.model.TeamScheduleRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeTeamScheduleRepository implements TeamScheduleRepository {

    private final Map<UUID, TeamSchedule> store = new HashMap<>();

    @Override
    public TeamSchedule save(TeamSchedule schedule) {
        if (schedule.getId() == null) {
            ReflectionTestUtils.setField(schedule, "id", UUID.randomUUID());
        }
        store.put(schedule.getId(), schedule);
        return schedule;
    }

    @Override
    public Optional<TeamSchedule> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void delete(TeamSchedule schedule) {
        store.remove(schedule.getId());
    }

    @Override
    public List<TeamSchedule> findAllByTeamIdOrderByScheduledAtAsc(UUID teamId) {
        return store.values().stream()
                .filter(s -> s.getTeam().getId().equals(teamId))
                .sorted(Comparator.comparing(TeamSchedule::getScheduledAt))
                .toList();
    }

    @Override
    public List<TeamSchedule> findAllByScheduledAtBetweenWithTeam(LocalDateTime start, LocalDateTime end) {
        return store.values().stream()
                .filter(s -> !s.getScheduledAt().isBefore(start) && !s.getScheduledAt().isAfter(end))
                .toList();
    }
}
