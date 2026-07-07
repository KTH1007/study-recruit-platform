package com.study.platform.domain.team.infrastructure

import com.study.platform.domain.team.model.StudyTeam
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface StudyTeamJpaRepository : JpaRepository<StudyTeam, UUID> {

    fun findByPostId(postId: UUID): StudyTeam?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM StudyTeam t WHERE t.id = :id")
    fun findByIdForUpdate(@Param("id") id: UUID): StudyTeam?
}
