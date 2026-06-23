package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.dto.response.NotificationResponse
import com.study.platform.domain.notification.port.NotificationQueryPort
import com.study.platform.domain.notification.usecase.FindNotificationsUseCase
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class FindNotificationsService(
    private val notificationQueryPort: NotificationQueryPort
) : FindNotificationsUseCase {

    override fun execute(userId: UUID, pageable: Pageable): Page<NotificationResponse> {
        return notificationQueryPort.findAllByReceiverId(userId, pageable)
    }
}
