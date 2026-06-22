package com.study.platform.domain.team.infrastructure

import com.study.platform.domain.team.model.TeamMember
import com.study.platform.domain.team.model.TeamMemberRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class TeamMemberRepositoryAdapter(
    private val teamMemberJpaRepository: TeamMemberJpaRepository
) : TeamMemberRepository {

    override fun save(member: TeamMember): TeamMember =
        teamMemberJpaRepository.save(member)

    override fun delete(member: TeamMember) {
        teamMemberJpaRepository.delete(member)
    }

    override fun findAllByTeamId(teamId: UUID): List<TeamMember> =
        teamMemberJpaRepository.findAllByTeamId(teamId)

    override fun findByTeamIdAndUserId(teamId: UUID, userId: UUID): TeamMember? =
        teamMemberJpaRepository.findByTeamIdAndUserId(teamId, userId)

    override fun existsByTeamIdAndUserId(teamId: UUID, userId: UUID): Boolean =
        teamMemberJpaRepository.existsByTeamIdAndUserId(teamId, userId)

    override fun countByTeamId(teamId: UUID): Long =
        teamMemberJpaRepository.countByTeamId(teamId)

    override fun deleteAllByTeamId(teamId: UUID) {
        teamMemberJpaRepository.deleteAllByTeamId(teamId)
    }
}
