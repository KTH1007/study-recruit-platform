package com.study.platform.domain.team.infrastructure

import com.study.platform.domain.team.model.TeamSchedule
import com.study.platform.domain.team.model.TeamScheduleRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class TeamScheduleRepositoryAdapter(
    private val teamScheduleJpaRepository: TeamScheduleJpaRepository
) : TeamScheduleRepository {

    override fun save(schedule: TeamSchedule): TeamSchedule =
        teamScheduleJpaRepository.save(schedule)

    override fun findById(id: UUID): TeamSchedule? =
        teamScheduleJpaRepository.findById(id).orElse(null)

    override fun delete(schedule: TeamSchedule) {
        teamScheduleJpaRepository.delete(schedule)
    }

    override fun deleteAllByTeamId(teamId: UUID) {
        teamScheduleJpaRepository.deleteAllByTeamId(teamId)
    }

    override fun findAllByScheduledAtBetweenWithTeam(start: LocalDateTime, end: LocalDateTime, pageable: Pageable): Slice<TeamSchedule> =
        teamScheduleJpaRepository.findAllByScheduledAtBetweenWithTeam(start, end, pageable)
}
