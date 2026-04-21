package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.model.Notification;
import com.study.platform.domain.notification.model.NotificationRepository;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private UUID receiverId;
    private UUID notificationId;
    private User receiver;
    private Notification notification;

    @BeforeEach
    void setUp() {
        receiverId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        receiver = User.create("kakao1", "수신자", "receiver@test.com");
        ReflectionTestUtils.setField(receiver, "id", receiverId);

        notification = Notification.create(receiver, NotificationType.APPLY_RECEIVED, "새 지원이 있습니다", UUID.randomUUID());
        ReflectionTestUtils.setField(notification, "id", notificationId);
    }

    @Test
    void countUnread_성공() {
        // given
        given(notificationRepository.countByReceiverIdAndIsReadFalse(receiverId)).willReturn(3L);

        // when
        long count = notificationService.countUnread(receiverId);

        // then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    void markAsRead_성공() {
        // given
        given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

        // when
        notificationService.markAsRead(receiverId, notificationId);

        // then
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void markAsRead_알림없음_예외발생() {
        // given
        given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.markAsRead(receiverId, notificationId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    void markAsRead_본인알림아님_예외발생() {
        // given
        UUID otherId = UUID.randomUUID();
        given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationService.markAsRead(otherId, notificationId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    void markAllAsRead_성공() {
        // when
        notificationService.markAllAsRead(receiverId);

        // then
        then(notificationRepository).should().markAllAsRead(receiverId);
    }
}
