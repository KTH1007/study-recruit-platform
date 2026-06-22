package com.study.platform.domain.notification.application

import com.study.platform.domain.notification.model.Notification
import com.study.platform.domain.notification.model.NotificationType
import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.support.fake.FakeNotificationQueryPort
import com.study.platform.support.fake.FakeNotificationRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.util.UUID

class NotificationServiceTest {

    private lateinit var notificationRepository: FakeNotificationRepository

    private lateinit var countUnreadNotificationsService: CountUnreadNotificationsService
    private lateinit var markNotificationAsReadService: MarkNotificationAsReadService
    private lateinit var markAllNotificationsAsReadService: MarkAllNotificationsAsReadService

    private lateinit var receiverId: UUID
    private lateinit var receiver: User
    private lateinit var notification: Notification

    @BeforeEach
    fun setUp() {
        notificationRepository = FakeNotificationRepository()

        countUnreadNotificationsService = CountUnreadNotificationsService(FakeNotificationQueryPort(notificationRepository))
        markNotificationAsReadService = MarkNotificationAsReadService(notificationRepository)
        markAllNotificationsAsReadService = MarkAllNotificationsAsReadService(notificationRepository)

        receiverId = UUID.randomUUID()

        receiver = User.create("kakao1", "수신자", "receiver@test.com")
        ReflectionTestUtils.setField(receiver, "id", receiverId)

        notification = Notification.create(receiver, NotificationType.APPLY_RECEIVED, "새 지원이 있습니다", UUID.randomUUID())
        ReflectionTestUtils.setField(notification, "id", UUID.randomUUID())
        notificationRepository.save(notification)
    }

    @Test
    fun `countUnread_성공`() {
        // given
        // notification is already saved in setUp as unread

        // when
        val count = countUnreadNotificationsService.execute(receiverId)

        // then
        assertThat(count).isEqualTo(1L)
    }

    @Test
    fun `markAsRead_성공`() {
        // given
        val notificationId = notification.id!!

        // when
        markNotificationAsReadService.execute(receiverId, notificationId)

        // then
        assertThat(notification.isRead).isTrue()
    }

    @Test
    fun `markAsRead_알림없음_예외발생`() {
        // given
        val nonExistentId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { markNotificationAsReadService.execute(receiverId, nonExistentId) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_NOT_FOUND)
    }

    @Test
    fun `markAsRead_본인알림아님_예외발생`() {
        // given
        val otherId = UUID.randomUUID()

        // when & then
        assertThatThrownBy { markNotificationAsReadService.execute(otherId, notification.id!!) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
    }

    @Test
    fun `markAllAsRead_성공`() {
        // given
        val another = Notification.create(receiver, NotificationType.COMMENT_CREATED, "새 댓글이 있습니다", UUID.randomUUID())
        ReflectionTestUtils.setField(another, "id", UUID.randomUUID())
        notificationRepository.save(another)

        // when
        markAllNotificationsAsReadService.execute(receiverId)

        // then
        assertThat(
            notificationRepository.findAllByReceiverId(receiverId)
                .stream().filter { n -> !n.isRead }.count()
        ).isZero()
    }
}
