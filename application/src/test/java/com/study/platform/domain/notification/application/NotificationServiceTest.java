package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.model.Notification;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.support.fake.FakeNotificationQueryPort;
import com.study.platform.support.fake.FakeNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationServiceTest {

    private FakeNotificationRepository notificationRepository;
    private NotificationService notificationService;

    private UUID receiverId;
    private User receiver;
    private Notification notification;

    @BeforeEach
    void setUp() {
        notificationRepository = new FakeNotificationRepository();
        notificationService = new NotificationService(notificationRepository, new FakeNotificationQueryPort(notificationRepository));

        receiverId = UUID.randomUUID();

        receiver = User.create("kakao1", "수신자", "receiver@test.com");
        ReflectionTestUtils.setField(receiver, "id", receiverId);

        notification = Notification.create(receiver, NotificationType.APPLY_RECEIVED, "새 지원이 있습니다", UUID.randomUUID());
        ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());
        notificationRepository.save(notification);
    }

    @Test
    void countUnread_성공() {
        // when
        long count = notificationService.countUnread(receiverId);

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void markAsRead_성공() {
        // when
        notificationService.markAsRead(receiverId, notification.getId());

        // then
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void markAsRead_알림없음_예외발생() {
        // when & then
        assertThatThrownBy(() -> notificationService.markAsRead(receiverId, UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    void markAsRead_본인알림아님_예외발생() {
        // given
        UUID otherId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> notificationService.markAsRead(otherId, notification.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    void markAllAsRead_성공() {
        // given
        Notification another = Notification.create(receiver, NotificationType.COMMENT_CREATED, "새 댓글이 있습니다", UUID.randomUUID());
        ReflectionTestUtils.setField(another, "id", UUID.randomUUID());
        notificationRepository.save(another);

        // when
        notificationService.markAllAsRead(receiverId);

        // then
        assertThat(notificationRepository.findAllByReceiverId(receiverId)
                .stream().filter(n -> !n.isRead()).count()).isZero();
    }
}
