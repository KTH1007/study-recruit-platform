package com.study.platform.support.fake

import com.study.platform.domain.post.model.FailedPostSync
import com.study.platform.domain.post.model.FailedPostSyncRepository

class FakeFailedPostSyncRepository : FailedPostSyncRepository {

    private val store: MutableList<FailedPostSync> = ArrayList()

    override fun save(failedPostSync: FailedPostSync): FailedPostSync {
        store.add(failedPostSync)
        return failedPostSync
    }

    override fun saveIndependently(failedPostSync: FailedPostSync): FailedPostSync = save(failedPostSync)

    fun getSaved(): List<FailedPostSync> = store
}
