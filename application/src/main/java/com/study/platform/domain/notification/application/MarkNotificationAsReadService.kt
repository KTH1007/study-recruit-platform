package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.model.NotificationRepository
import com.study.platform.domain.notification.usecase.MarkNotificationAsReadUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class MarkNotificationAsReadService(
    private val notificationRepository: NotificationRepository
) : MarkNotificationAsReadUseCase {

    @Transactional
    override fun execute(userId: UUID, notificationId: UUID) {
        val notification = notificationRepository.findById(notificationId)
            ?: throw CustomException(ErrorCode.NOTIFICATION_NOT_FOUND)
        notification.validateReceiver(userId)
        notification.markAsRead()
    }
}
