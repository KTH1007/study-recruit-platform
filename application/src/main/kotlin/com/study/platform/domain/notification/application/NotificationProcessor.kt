package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.model.FailedNotification
import com.study.platform.domain.notification.model.FailedNotificationRepository
import com.study.platform.domain.notification.model.Notification
import com.study.platform.domain.notification.model.NotificationEvent
import com.study.platform.domain.notification.model.NotificationRepository
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.constant.KafkaConstants
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class NotificationProcessor(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val failedNotificationRepository: FailedNotificationRepository
) {
    @Transactional
    fun process(event: NotificationEvent): Notification? {
        if (event.retryCount >= KafkaConstants.MAX_DLT_RETRY) {
            failedNotificationRepository.save(FailedNotification.from(event, "메인 Consumer 최종 실패"))
            return null
        }
        val receiver = userRepository.findById(event.receiverId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        val notification = Notification.create(receiver, event.type, event.message, event.targetId)
        return notificationRepository.save(notification)
    }
}
