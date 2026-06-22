package com.study.platform.support.fake

import com.study.platform.domain.apply.model.Apply
import com.study.platform.domain.apply.model.ApplyRepository
import com.study.platform.domain.apply.model.ApplyStatus
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.test.util.ReflectionTestUtils
import java.util.UUID

class FakeApplyRepository : ApplyRepository {

    private val store: MutableMap<UUID, Apply> = HashMap()

    override fun save(apply: Apply): Apply {
        if (apply.id == null) {
            ReflectionTestUtils.setField(apply, "id", UUID.randomUUID())
        }
        val duplicate = store.values.stream()
            .filter { a -> a.id != apply.id }
            .anyMatch { a ->
                a.post.id == apply.post.id &&
                    a.applicant.id == apply.applicant.id
            }
        if (duplicate) {
            throw CustomException(ErrorCode.ALREADY_APPLIED)
        }
        store[apply.id!!] = apply
        return apply
    }

    override fun delete(apply: Apply) {
        store.remove(apply.id)
    }

    override fun findById(id: UUID): Apply? = store[id]

    override fun existsByPostIdAndApplicantId(postId: UUID, applicantId: UUID): Boolean =
        store.values.any { a -> a.post.id == postId && a.applicant.id == applicantId }

    fun findAllByPostId(postId: UUID): List<Apply> =
        store.values.filter { a -> a.post.id == postId }

    override fun findByIdWithPostAndApplicant(applyId: UUID): Apply? = store[applyId]

    override fun findByPostIdAndApplicantId(postId: UUID, applicantId: UUID): Apply? =
        store.values.firstOrNull { a ->
            a.post.id == postId && a.applicant.id == applicantId
        }

    override fun countByPostIdAndStatus(postId: UUID, status: ApplyStatus): Long =
        store.values.count { a ->
            a.post.id == postId && a.status == status
        }.toLong()

    override fun findByIdWithPostAndApplicantForUpdate(applyId: UUID): Apply? = store[applyId]
}
