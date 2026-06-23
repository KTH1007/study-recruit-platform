package com.study.platform.domain.apply.infrastructure

import com.study.platform.domain.apply.model.ApplyStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ApplyJpaRepository : JpaRepository<ApplyJpaEntity, UUID> {

    fun existsByPostIdAndApplicantId(postId: UUID, applicantId: UUID): Boolean

    @Query("SELECT a FROM ApplyJpaEntity a JOIN FETCH a.post JOIN FETCH a.applicant WHERE a.id = :applyId")
    fun findByIdWithPostAndApplicant(@Param("applyId") applyId: UUID): ApplyJpaEntity?

    fun findByPostIdAndApplicantId(postId: UUID, applicantId: UUID): ApplyJpaEntity?

    fun countByPostIdAndStatus(postId: UUID, status: ApplyStatus): Long

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ApplyJpaEntity a JOIN FETCH a.post JOIN FETCH a.applicant WHERE a.id = :applyId")
    fun findByIdWithPostAndApplicantForUpdate(@Param("applyId") applyId: UUID): ApplyJpaEntity?
}
