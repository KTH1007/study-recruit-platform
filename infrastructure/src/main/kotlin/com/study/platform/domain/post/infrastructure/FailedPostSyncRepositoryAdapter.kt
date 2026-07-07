package com.study.platform.domain.post.infrastructure

import com.study.platform.domain.post.model.FailedPostSync
import com.study.platform.domain.post.model.FailedPostSyncRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Repository
class FailedPostSyncRepositoryAdapter(
    private val failedPostSyncJpaRepository: FailedPostSyncJpaRepository
) : FailedPostSyncRepository {

    override fun save(failedPostSync: FailedPostSync): FailedPostSync =
        failedPostSyncJpaRepository.save(failedPostSync)

    // 스킵 리스너처럼 이미 롤백이 확정된 트랜잭션 안에서 호출되는 경우를 위해 독립 트랜잭션으로 커밋한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun saveIndependently(failedPostSync: FailedPostSync): FailedPostSync =
        failedPostSyncJpaRepository.save(failedPostSync)
}
