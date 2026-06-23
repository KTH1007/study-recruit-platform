package com.study.platform.domain.notification.infrastructure

import com.study.platform.domain.notification.model.FailedNotification
import com.study.platform.domain.notification.model.FailedNotificationRepository
import org.springframework.stereotype.Repository

@Repository
class FailedNotificationRepositoryAdapter(
    private val failedNotificationJpaRepository: FailedNotificationJpaRepository
) : FailedNotificationRepository {

    override fun save(failedNotification: FailedNotification): FailedNotification =
        failedNotificationJpaRepository.save(failedNotification)
}
