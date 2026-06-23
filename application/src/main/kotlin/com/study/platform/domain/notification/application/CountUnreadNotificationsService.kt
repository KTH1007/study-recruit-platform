package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.port.NotificationQueryPort
import com.study.platform.domain.notification.usecase.CountUnreadNotificationsUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class CountUnreadNotificationsService(
    private val notificationQueryPort: NotificationQueryPort
) : CountUnreadNotificationsUseCase {

    override fun execute(userId: UUID): Long {
        return notificationQueryPort.countByReceiverIdAndIsReadFalse(userId)
    }
}
