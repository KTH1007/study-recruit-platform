package com.study.platform.domain.team.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamScheduleRepository {

    TeamSchedule save(TeamSchedule schedule);
    Optional<TeamSchedule> findById(UUID id);
    void delete(TeamSchedule schedule);
    List<TeamSchedule> findAllByScheduledAtBetweenWithTeam(LocalDateTime start, LocalDateTime end);
}
