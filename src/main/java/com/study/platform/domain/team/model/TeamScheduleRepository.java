package com.study.platform.domain.team.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TeamScheduleRepository extends JpaRepository<TeamSchedule, UUID> {

    List<TeamSchedule> findAllByTeamIdOrderByScheduledAtAsc(UUID teamId);

    @Query("SELECT s FROM TeamSchedule s JOIN FETCH s.team WHERE s.scheduledAt BETWEEN :start AND :end")
    List<TeamSchedule> findAllByScheduledAtBetweenWithTeam(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
