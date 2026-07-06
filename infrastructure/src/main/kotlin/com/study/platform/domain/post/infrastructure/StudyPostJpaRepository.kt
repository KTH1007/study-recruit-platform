package com.study.platform.domain.post.infrastructure

import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.post.model.StudyPostStatus
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.UUID

interface StudyPostJpaRepository : JpaRepository<StudyPost, UUID> {

    @Query("SELECT p FROM StudyPost p JOIN FETCH p.author WHERE p.id = :postId")
    fun findByIdWithAuthor(@Param("postId") postId: UUID): StudyPost?

    @Query("SELECT p FROM StudyPost p WHERE p.status = :status AND p.deadline BETWEEN :start AND :end")
    fun findDeadlineReminderPosts(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime,
        @Param("status") status: StudyPostStatus
    ): List<StudyPost>

    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM StudyPost p WHERE p.id = :postId")
    fun findByIdWithAuthorForUpdate(@Param("postId") postId: UUID): StudyPost?
}
