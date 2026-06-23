package com.study.platform.domain.notification.model

interface FailedNotificationRepository {
    fun save(failedNotification: FailedNotification): FailedNotification
}
