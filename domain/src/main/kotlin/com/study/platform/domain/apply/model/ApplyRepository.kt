package com.study.platform.domain.apply.model

import java.util.UUID

interface ApplyRepository {

    fun save(apply: Apply): Apply
    fun delete(apply: Apply)
    fun deleteAllByPostId(postId: UUID)
    fun findById(id: UUID): Apply?

    fun existsByPostIdAndApplicantId(postId: UUID, applicantId: UUID): Boolean
    fun findByPostIdAndApplicantId(postId: UUID, applicantId: UUID): Apply?
    fun countByPostIdAndStatus(postId: UUID, status: ApplyStatus): Long
    fun countByPostIdAndStatusForUpdate(postId: UUID, status: ApplyStatus): Long
    fun findByIdWithPostAndApplicantForUpdate(applyId: UUID): Apply?
    fun findByIdForUpdate(applyId: UUID): Apply?
    fun findPostIdByApplyId(applyId: UUID): UUID?
}
