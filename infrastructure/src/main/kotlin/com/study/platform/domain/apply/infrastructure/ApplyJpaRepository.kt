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

    fun deleteAllByPostId(postId: UUID)

    fun findByPostIdAndApplicantId(postId: UUID, applicantId: UUID): ApplyJpaEntity?

    fun countByPostIdAndStatus(postId: UUID, status: ApplyStatus): Long

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT COUNT(a) FROM ApplyJpaEntity a WHERE a.post.id = :postId AND a.status = :status")
    fun countByPostIdAndStatusForUpdate(@Param("postId") postId: UUID, @Param("status") status: ApplyStatus): Long

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ApplyJpaEntity a JOIN FETCH a.post WHERE a.id = :applyId")
    fun findByIdWithPostAndApplicantForUpdate(@Param("applyId") applyId: UUID): ApplyJpaEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ApplyJpaEntity a WHERE a.id = :applyId")
    fun findByIdForUpdate(@Param("applyId") applyId: UUID): ApplyJpaEntity?

    @Query("SELECT a.post.id FROM ApplyJpaEntity a WHERE a.id = :applyId")
    fun findPostIdByApplyId(@Param("applyId") applyId: UUID): UUID?
}
