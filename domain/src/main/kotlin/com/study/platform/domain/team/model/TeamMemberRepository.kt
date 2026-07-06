package com.study.platform.domain.team.model

import java.util.UUID

interface TeamMemberRepository {

    fun save(member: TeamMember): TeamMember
    fun delete(member: TeamMember)
    fun findAllByTeamId(teamId: UUID): List<TeamMember>
    fun findAllByTeamIdIn(teamIds: Collection<UUID>): List<TeamMember>
    fun findByTeamIdAndUserId(teamId: UUID, userId: UUID): TeamMember?
    fun existsByTeamIdAndUserId(teamId: UUID, userId: UUID): Boolean
    fun countByTeamId(teamId: UUID): Long
    fun deleteAllByTeamId(teamId: UUID)
}
