package com.study.platform.support.fake

import com.study.platform.domain.team.model.TeamSchedule
import com.study.platform.domain.team.model.TeamScheduleRepository
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.UUID


class FakeTeamScheduleRepository : TeamScheduleRepository {

    private val store: MutableMap<UUID, TeamSchedule> = HashMap()

    override fun save(schedule: TeamSchedule): TeamSchedule {
        if (schedule.id == null) {
            ReflectionTestUtils.setField(schedule, "id", UUID.randomUUID())
        }
        if (schedule.createdAt == null) {
            ReflectionTestUtils.setField(schedule, "createdAt", LocalDateTime.now())
        }
        ReflectionTestUtils.setField(schedule, "updatedAt", LocalDateTime.now())
        store[schedule.id!!] = schedule
        return schedule
    }

    override fun findById(id: UUID): TeamSchedule? = store[id]

    override fun delete(schedule: TeamSchedule) {
        store.remove(schedule.id)
    }

    fun findAllByTeamId(teamId: UUID): List<TeamSchedule> =
        store.values
            .filter { s -> s.team?.id == teamId }
            .sortedBy { it.scheduledAt }

    override fun findAllByScheduledAtBetweenWithTeam(start: LocalDateTime, end: LocalDateTime): List<TeamSchedule> =
        store.values.filter { s -> s.scheduledAt?.let { !it.isBefore(start) && !it.isAfter(end) } == true }
}
