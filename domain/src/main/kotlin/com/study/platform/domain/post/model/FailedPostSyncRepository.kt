package com.study.platform.domain.post.model

interface FailedPostSyncRepository {
    fun save(failedPostSync: FailedPostSync): FailedPostSync
}
