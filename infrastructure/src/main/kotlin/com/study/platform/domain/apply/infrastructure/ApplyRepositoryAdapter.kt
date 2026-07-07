package com.study.platform.domain.apply.infrastructure

import com.study.platform.domain.apply.model.Apply
import com.study.platform.domain.apply.model.ApplyRepository
import com.study.platform.domain.apply.model.ApplyStatus
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ApplyRepositoryAdapter(
    private val applyJpaRepository: ApplyJpaRepository,
    private val applyMapper: ApplyMapper
) : ApplyRepository {

    override fun save(apply: Apply): Apply {
        return try {
            applyMapper.toDomain(applyJpaRepository.save(applyMapper.toEntity(apply)))
        } catch (e: DataIntegrityViolationException) {
            throw CustomException(ErrorCode.ALREADY_APPLIED)
        }
    }

    override fun delete(apply: Apply) {
        applyJpaRepository.deleteById(apply.id)
    }

    override fun deleteAllByPostId(postId: UUID) {
        applyJpaRepository.deleteAllByPostId(postId)
    }

    override fun findById(id: UUID): Apply? =
        applyJpaRepository.findById(id).orElse(null)?.let(applyMapper::toDomain)

    override fun existsByPostIdAndApplicantId(postId: UUID, applicantId: UUID): Boolean =
        applyJpaRepository.existsByPostIdAndApplicantId(postId, applicantId)

    override fun findByPostIdAndApplicantId(postId: UUID, applicantId: UUID): Apply? =
        applyJpaRepository.findByPostIdAndApplicantId(postId, applicantId)?.let(applyMapper::toDomain)

    override fun countByPostIdAndStatus(postId: UUID, status: ApplyStatus): Long =
        applyJpaRepository.countByPostIdAndStatus(postId, status)

    override fun countByPostIdAndStatusForUpdate(postId: UUID, status: ApplyStatus): Long =
        applyJpaRepository.countByPostIdAndStatusForUpdate(postId, status)

    override fun findByIdWithPostAndApplicantForUpdate(applyId: UUID): Apply? =
        applyJpaRepository.findByIdWithPostAndApplicantForUpdate(applyId)?.let(applyMapper::toDomain)

    override fun findPostIdByApplyId(applyId: UUID): UUID? =
        applyJpaRepository.findPostIdByApplyId(applyId)
}
