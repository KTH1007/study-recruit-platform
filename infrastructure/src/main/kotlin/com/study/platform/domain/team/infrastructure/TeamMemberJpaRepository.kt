package com.study.platform.domain.team.infrastructure

import com.study.platform.domain.team.model.TeamMember
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TeamMemberJpaRepository : JpaRepository<TeamMember, UUID> {

    fun findAllByTeamId(teamId: UUID): List<TeamMember>

    fun findByTeamIdAndUserId(teamId: UUID, userId: UUID): TeamMember?

    fun existsByTeamIdAndUserId(teamId: UUID, userId: UUID): Boolean

    fun countByTeamId(teamId: UUID): Long

    fun deleteAllByTeamId(teamId: UUID)
}
