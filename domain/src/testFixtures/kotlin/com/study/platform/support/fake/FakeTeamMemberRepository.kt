package com.study.platform.support.fake

import com.study.platform.domain.team.model.TeamMember
import com.study.platform.domain.team.model.TeamMemberRepository
import java.util.UUID

class FakeTeamMemberRepository : AbstractFakeUuidRepository<TeamMember>(), TeamMemberRepository {

    override fun idOf(entity: TeamMember): UUID? = entity.id

    override fun save(member: TeamMember): TeamMember = saveEntity(member)

    override fun delete(member: TeamMember) = deleteEntity(member)

    override fun findAllByTeamId(teamId: UUID): List<TeamMember> =
        store.values.filter { m -> m.team?.id == teamId }

    override fun findAllByTeamIdIn(teamIds: Collection<UUID>): List<TeamMember> =
        store.values.filter { m -> teamIds.contains(m.team?.id) }

    override fun findByTeamIdAndUserId(teamId: UUID, userId: UUID): TeamMember? =
        store.values.firstOrNull { m -> m.team?.id == teamId && m.user?.id == userId }

    override fun existsByTeamIdAndUserId(teamId: UUID, userId: UUID): Boolean =
        findByTeamIdAndUserId(teamId, userId) != null

    override fun countByTeamId(teamId: UUID): Long =
        store.values.count { m -> m.team?.id == teamId }.toLong()

    override fun deleteAllByTeamId(teamId: UUID) {
        val toRemove = store.values
            .filter { m -> m.team?.id == teamId }
            .mapNotNull { it.id }
        toRemove.forEach { store.remove(it) }
    }
}
