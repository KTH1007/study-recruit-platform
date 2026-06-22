package com.study.platform.support.fake

import com.study.platform.domain.team.model.TeamMember
import com.study.platform.domain.team.model.TeamMemberRepository
import org.springframework.test.util.ReflectionTestUtils
import java.util.UUID

class FakeTeamMemberRepository : TeamMemberRepository {

    private val store: MutableMap<UUID, TeamMember> = HashMap()

    override fun save(member: TeamMember): TeamMember {
        if (member.id == null) {
            ReflectionTestUtils.setField(member, "id", UUID.randomUUID())
        }
        store[member.id!!] = member
        return member
    }

    override fun delete(member: TeamMember) {
        store.remove(member.id)
    }

    override fun findAllByTeamId(teamId: UUID): List<TeamMember> =
        store.values.filter { m -> m.team?.id == teamId }

    override fun findByTeamIdAndUserId(teamId: UUID, userId: UUID): TeamMember? =
        store.values.firstOrNull { m -> m.team?.id == teamId && m.user?.id == userId }

    override fun existsByTeamIdAndUserId(teamId: UUID, userId: UUID): Boolean =
        findByTeamIdAndUserId(teamId, userId) != null

    override fun countByTeamId(teamId: UUID): Long =
        store.values.count { m -> m.team?.id == teamId }.toLong()

    override fun deleteAllByTeamId(teamId: UUID) {
        val toRemove = store.values
            .filter { m -> m.team?.id == teamId }
            .map { it.id!! }
        toRemove.forEach { store.remove(it) }
    }
}
