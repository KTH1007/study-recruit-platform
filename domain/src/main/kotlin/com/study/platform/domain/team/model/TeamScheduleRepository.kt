package com.study.platform.domain.team.model

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import java.time.LocalDateTime
import java.util.UUID

interface TeamScheduleRepository {

    fun save(schedule: TeamSchedule): TeamSchedule
    fun findById(id: UUID): TeamSchedule?
    fun delete(schedule: TeamSchedule)
    fun deleteAllByTeamId(teamId: UUID)
    fun findAllByScheduledAtBetweenWithTeam(start: LocalDateTime, end: LocalDateTime, pageable: Pageable): Slice<TeamSchedule>
}
