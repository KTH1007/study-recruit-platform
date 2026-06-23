package com.study.platform.domain.notification.usecase

import java.util.UUID

interface MarkNotificationAsReadUseCase {
    fun execute(userId: UUID, notificationId: UUID)
}
