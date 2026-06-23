package com.study.platform.support.fake

import com.study.platform.domain.team.model.StudyTeam
import com.study.platform.domain.team.model.StudyTeamRepository
import org.springframework.test.util.ReflectionTestUtils
import java.util.UUID

class FakeStudyTeamRepository : StudyTeamRepository {

    private val store: MutableMap<UUID, StudyTeam> = HashMap()

    override fun save(team: StudyTeam): StudyTeam {
        if (team.id == null) {
            ReflectionTestUtils.setField(team, "id", UUID.randomUUID())
        }
        store[team.id!!] = team
        return team
    }

    override fun findById(id: UUID): StudyTeam? = store[id]

    override fun delete(team: StudyTeam) {
        store.remove(team.id)
    }

    override fun findByPostId(postId: UUID): StudyTeam? =
        store.values.firstOrNull { t -> t.post?.id == postId }
}
