package com.study.platform.support.fake

import com.study.platform.domain.apply.model.Apply
import com.study.platform.domain.apply.model.ApplyRepository
import com.study.platform.domain.apply.model.ApplyStatus
import java.util.UUID

class FakeApplyRepository : AbstractFakeUuidRepository<Apply>(), ApplyRepository {

    override fun idOf(entity: Apply): UUID? = entity.id

    override fun save(apply: Apply): Apply = saveEntity(apply)

    override fun delete(apply: Apply) = deleteEntity(apply)

    override fun deleteAllByPostId(postId: UUID) {
        findAllByPostId(postId).forEach { store.remove(it.id) }
    }

    override fun existsByPostIdAndApplicantId(postId: UUID, applicantId: UUID): Boolean =
        store.values.any { a -> a.post.id == postId && a.applicant.id == applicantId }

    fun findAllByPostId(postId: UUID): List<Apply> =
        store.values.filter { a -> a.post.id == postId }

    override fun findByPostIdAndApplicantId(postId: UUID, applicantId: UUID): Apply? =
        store.values.firstOrNull { a ->
            a.post.id == postId && a.applicant.id == applicantId
        }

    override fun countByPostIdAndStatus(postId: UUID, status: ApplyStatus): Long =
        store.values.count { a ->
            a.post.id == postId && a.status == status
        }.toLong()

    override fun countByPostIdAndStatusForUpdate(postId: UUID, status: ApplyStatus): Long =
        countByPostIdAndStatus(postId, status)

    override fun findByIdWithPostAndApplicantForUpdate(applyId: UUID): Apply? = store[applyId]

    override fun findPostIdByApplyId(applyId: UUID): UUID? = store[applyId]?.post?.id
}
