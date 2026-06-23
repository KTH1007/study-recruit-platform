package com.study.platform.domain.team.model

import java.util.UUID

interface StudyTeamRepository {

    fun save(team: StudyTeam): StudyTeam
    fun findById(id: UUID): StudyTeam?
    fun delete(team: StudyTeam)
    fun findByPostId(postId: UUID): StudyTeam?
}
