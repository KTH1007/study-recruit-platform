package com.study.platform.domain.team.infrastructure

import com.study.platform.domain.team.model.TeamSchedule
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.UUID

interface TeamScheduleJpaRepository : JpaRepository<TeamSchedule, UUID> {

    fun deleteAllByTeamId(teamId: UUID)

    @Query("SELECT s FROM TeamSchedule s JOIN FETCH s.team WHERE s.scheduledAt BETWEEN :start AND :end ORDER BY s.id")
    fun findAllByScheduledAtBetweenWithTeam(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime,
        pageable: Pageable
    ): Slice<TeamSchedule>
}
