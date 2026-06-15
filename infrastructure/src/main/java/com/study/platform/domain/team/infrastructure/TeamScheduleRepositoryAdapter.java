package com.study.platform.domain.team.infrastructure;

import com.study.platform.domain.team.model.TeamSchedule;
import com.study.platform.domain.team.model.TeamScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TeamScheduleRepositoryAdapter implements TeamScheduleRepository {

    private final TeamScheduleJpaRepository teamScheduleJpaRepository;

    @Override
    public TeamSchedule save(TeamSchedule schedule) {
        return teamScheduleJpaRepository.save(schedule);
    }

    @Override
    public Optional<TeamSchedule> findById(UUID id) {
        return teamScheduleJpaRepository.findById(id);
    }

    @Override
    public void delete(TeamSchedule schedule) {
        teamScheduleJpaRepository.delete(schedule);
    }

    @Override
    public List<TeamSchedule> findAllByScheduledAtBetweenWithTeam(LocalDateTime start, LocalDateTime end) {
        return teamScheduleJpaRepository.findAllByScheduledAtBetweenWithTeam(start, end);
    }
}
