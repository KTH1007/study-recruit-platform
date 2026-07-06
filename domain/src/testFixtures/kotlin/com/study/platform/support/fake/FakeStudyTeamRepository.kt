package com.study.platform.support.fake

import com.study.platform.domain.team.model.StudyTeam
import com.study.platform.domain.team.model.StudyTeamRepository
import java.util.UUID

class FakeStudyTeamRepository : AbstractFakeUuidRepository<StudyTeam>(), StudyTeamRepository {

    override fun idOf(entity: StudyTeam): UUID? = entity.id

    override fun save(team: StudyTeam): StudyTeam = saveEntity(team)

    override fun delete(team: StudyTeam) = deleteEntity(team)

    override fun findByPostId(postId: UUID): StudyTeam? =
        store.values.firstOrNull { t -> t.post?.id == postId }
}
