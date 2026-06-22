package com.study.platform.domain.notification.model

import com.study.platform.domain.notification.dto.response.NotificationResponse
import com.study.platform.domain.notification.port.NotificationQueryPort
import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.support.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
class NotificationRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var notificationRepository: NotificationRepository

    @Autowired
    private lateinit var notificationQueryPort: NotificationQueryPort

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var receiver: User
    private lateinit var other: User

    @BeforeEach
    fun setUp() {
        receiver = userRepository.save(User.create("kakao-1", "수신자", "receiver@test.com"))
        other = userRepository.save(User.create("kakao-2", "다른유저", "other@test.com"))
        em.flush()
    }

    @Test
    fun `findAllByReceiverId_성공`() {
        notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_RECEIVED, "지원이 왔습니다", UUID.randomUUID()))
        notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_APPROVED, "승인되었습니다", UUID.randomUUID()))
        notificationRepository.save(Notification.create(other, NotificationType.APPLY_RECEIVED, "다른 유저 알림", UUID.randomUUID()))
        em.flush()

        val result: Page<NotificationResponse> = notificationQueryPort.findAllByReceiverId(receiver.id!!, PageRequest.of(0, 20))

        assertThat(result.content).hasSize(2)
        assertThat(result.content).allMatch { n -> n.receiverId == receiver.id }
    }

    @Test
    fun `findAllByReceiverId_알림없음_빈페이지`() {
        val result: Page<NotificationResponse> = notificationQueryPort.findAllByReceiverId(receiver.id!!, PageRequest.of(0, 20))

        assertThat(result.content).isEmpty()
    }

    @Test
    fun `countByReceiverIdAndIsReadFalse_읽지않은알림_카운트`() {
        val read = notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_RECEIVED, "읽은 알림", UUID.randomUUID()))
        read.markAsRead()
        notificationRepository.save(read)
        notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_APPROVED, "안 읽은 알림", UUID.randomUUID()))
        em.flush()

        val count = notificationQueryPort.countByReceiverIdAndIsReadFalse(receiver.id!!)

        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `countByReceiverIdAndIsReadFalse_모두읽음_0`() {
        val n = notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_RECEIVED, "알림", UUID.randomUUID()))
        n.markAsRead()
        notificationRepository.save(n)

        val count = notificationQueryPort.countByReceiverIdAndIsReadFalse(receiver.id!!)

        assertThat(count).isZero()
    }

    @Test
    fun `markAllAsRead_안읽은알림_모두읽음처리`() {
        notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_RECEIVED, "알림1", UUID.randomUUID()))
        notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_APPROVED, "알림2", UUID.randomUUID()))
        em.flush()

        notificationRepository.markAllAsRead(receiver.id!!)

        val unreadCount = notificationQueryPort.countByReceiverIdAndIsReadFalse(receiver.id!!)
        assertThat(unreadCount).isZero()
    }

    @Test
    fun `markAllAsRead_다른유저알림_영향없음`() {
        notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_RECEIVED, "수신자 알림", UUID.randomUUID()))
        notificationRepository.save(Notification.create(other, NotificationType.APPLY_RECEIVED, "다른유저 알림", UUID.randomUUID()))
        em.flush()

        notificationRepository.markAllAsRead(receiver.id!!)

        val otherUnreadCount = notificationQueryPort.countByReceiverIdAndIsReadFalse(other.id!!)
        assertThat(otherUnreadCount).isEqualTo(1)
    }
}
