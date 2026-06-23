package com.study.platform.domain.team.model

import java.time.LocalDateTime
import java.util.UUID

interface TeamScheduleRepository {

    fun save(schedule: TeamSchedule): TeamSchedule
    fun findById(id: UUID): TeamSchedule?
    fun delete(schedule: TeamSchedule)
    fun findAllByScheduledAtBetweenWithTeam(start: LocalDateTime, end: LocalDateTime): List<TeamSchedule>
}
