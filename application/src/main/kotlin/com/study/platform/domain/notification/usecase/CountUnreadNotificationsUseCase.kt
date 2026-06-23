package com.study.platform.domain.notification.usecase

import java.util.UUID

interface CountUnreadNotificationsUseCase {
    fun execute(userId: UUID): Long
}
