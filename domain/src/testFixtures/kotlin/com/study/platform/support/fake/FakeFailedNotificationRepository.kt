package com.study.platform.support.fake

import com.study.platform.domain.notification.model.FailedNotification
import com.study.platform.domain.notification.model.FailedNotificationRepository

class FakeFailedNotificationRepository : FailedNotificationRepository {

    private val store: MutableList<FailedNotification> = ArrayList()

    override fun save(failedNotification: FailedNotification): FailedNotification {
        store.add(failedNotification)
        return failedNotification
    }

    fun getSaved(): List<FailedNotification> = store
}
