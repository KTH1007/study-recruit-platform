package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.model.NotificationRepository
import com.study.platform.domain.notification.usecase.MarkAllNotificationsAsReadUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class MarkAllNotificationsAsReadService(
    private val notificationRepository: NotificationRepository
) : MarkAllNotificationsAsReadUseCase {

    @Transactional
    override fun execute(userId: UUID) {
        notificationRepository.markAllAsRead(userId)
    }
}
