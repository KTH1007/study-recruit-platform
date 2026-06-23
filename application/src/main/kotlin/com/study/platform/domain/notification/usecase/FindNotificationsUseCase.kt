package com.study.platform.domain.notification.usecase

import com.study.platform.domain.notification.dto.response.NotificationResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface FindNotificationsUseCase {
    fun execute(userId: UUID, pageable: Pageable): Page<NotificationResponse>
}
