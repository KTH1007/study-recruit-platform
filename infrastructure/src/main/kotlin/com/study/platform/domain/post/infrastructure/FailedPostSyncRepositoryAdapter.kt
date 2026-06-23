package com.study.platform.domain.post.infrastructure

import com.study.platform.domain.post.model.FailedPostSync
import com.study.platform.domain.post.model.FailedPostSyncRepository
import org.springframework.stereotype.Repository

@Repository
class FailedPostSyncRepositoryAdapter(
    private val failedPostSyncJpaRepository: FailedPostSyncJpaRepository
) : FailedPostSyncRepository {

    override fun save(failedPostSync: FailedPostSync): FailedPostSync =
        failedPostSyncJpaRepository.save(failedPostSync)
}
