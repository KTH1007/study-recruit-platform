package com.study.platform.domain.team.infrastructure

import com.study.platform.domain.team.model.TeamSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.UUID

interface TeamScheduleJpaRepository : JpaRepository<TeamSchedule, UUID> {

    @Query("SELECT s FROM TeamSchedule s JOIN FETCH s.team WHERE s.scheduledAt BETWEEN :start AND :end")
    fun findAllByScheduledAtBetweenWithTeam(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<TeamSchedule>
}
