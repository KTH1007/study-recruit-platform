package com.study.platform.domain.team.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TeamScheduleRepository extends JpaRepository<TeamSchedule, UUID> {

    List<TeamSchedule> findAllByTeamIdOrderByScheduledAtAsc(UUID teamId);

    List<TeamSchedule> findAllByScheduledAtBetween(LocalDateTime start, LocalDateTime end);
}
