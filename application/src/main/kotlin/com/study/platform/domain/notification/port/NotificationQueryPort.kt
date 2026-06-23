package com.study.platform.domain.notification.port

import com.study.platform.domain.notification.dto.response.NotificationResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface NotificationQueryPort {
    fun findAllByReceiverId(receiverId: UUID, pageable: Pageable): Page<NotificationResponse>
    fun countByReceiverIdAndIsReadFalse(receiverId: UUID): Long
}
