package com.study.platform.domain.team.infrastructure

import com.study.platform.domain.team.model.StudyTeam
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StudyTeamJpaRepository : JpaRepository<StudyTeam, UUID> {

    fun findByPostId(postId: UUID): StudyTeam?
}
