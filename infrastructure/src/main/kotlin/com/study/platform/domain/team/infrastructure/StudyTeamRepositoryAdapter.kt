package com.study.platform.domain.team.infrastructure

import com.study.platform.domain.team.model.StudyTeam
import com.study.platform.domain.team.model.StudyTeamRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class StudyTeamRepositoryAdapter(
    private val studyTeamJpaRepository: StudyTeamJpaRepository
) : StudyTeamRepository {

    override fun save(team: StudyTeam): StudyTeam =
        studyTeamJpaRepository.save(team)

    override fun findById(id: UUID): StudyTeam? =
        studyTeamJpaRepository.findById(id).orElse(null)

    override fun delete(team: StudyTeam) {
        studyTeamJpaRepository.delete(team)
    }

    override fun findByPostId(postId: UUID): StudyTeam? =
        studyTeamJpaRepository.findByPostId(postId)

    override fun findByIdForUpdate(id: UUID): StudyTeam? =
        studyTeamJpaRepository.findByIdForUpdate(id)
}
