package com.study.platform.support.fake

import com.study.platform.domain.notification.dto.response.NotificationResponse
import com.study.platform.domain.notification.port.NotificationQueryPort
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

class FakeNotificationQueryPort(
    private val repository: FakeNotificationRepository
) : NotificationQueryPort {

    override fun findAllByReceiverId(receiverId: UUID, pageable: Pageable): Page<NotificationResponse> {
        throw UnsupportedOperationException("필요 시 구현")
    }

    override fun countByReceiverIdAndIsReadFalse(receiverId: UUID): Long =
        repository.countByReceiverIdAndIsReadFalse(receiverId)
}
