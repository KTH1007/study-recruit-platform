package com.study.platform.domain.notification.usecase

import java.util.UUID

interface MarkAllNotificationsAsReadUseCase {
    fun execute(userId: UUID)
}
