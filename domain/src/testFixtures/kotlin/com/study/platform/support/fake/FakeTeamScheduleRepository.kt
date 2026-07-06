package com.study.platform.support.fake

import com.study.platform.domain.team.model.TeamSchedule
import com.study.platform.domain.team.model.TeamScheduleRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import java.time.LocalDateTime
import java.util.UUID

class FakeTeamScheduleRepository : AbstractFakeUuidRepository<TeamSchedule>(), TeamScheduleRepository {

    override fun idOf(entity: TeamSchedule): UUID? = entity.id
    override fun hasTimestamps() = true

    override fun save(schedule: TeamSchedule): TeamSchedule = saveEntity(schedule)

    override fun delete(schedule: TeamSchedule) = deleteEntity(schedule)

    override fun deleteAllByTeamId(teamId: UUID) {
        store.values.filter { it.team?.id == teamId }.mapNotNull { it.id }.forEach { store.remove(it) }
    }

    fun findAllByTeamId(teamId: UUID): List<TeamSchedule> =
        store.values
            .filter { s -> s.team?.id == teamId }
            .sortedBy { it.scheduledAt }

    override fun findAllByScheduledAtBetweenWithTeam(start: LocalDateTime, end: LocalDateTime, pageable: Pageable): Slice<TeamSchedule> {
        val matched = store.values
            .filter { s -> s.scheduledAt?.let { !it.isBefore(start) && !it.isAfter(end) } == true }
            .sortedBy { it.id.toString() }
        val content = matched.drop(pageable.offset.toInt()).take(pageable.pageSize)
        val hasNext = pageable.offset + content.size < matched.size
        return SliceImpl(content, pageable, hasNext)
    }
}
