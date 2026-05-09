package com.study.platform.domain.notification.model;

import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.study.platform.global.support.AbstractIntegrationTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class NotificationRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User receiver;
    private User other;

    @BeforeEach
    void setUp() {
        receiver = userRepository.save(User.create("kakao-1", "수신자", "receiver@test.com"));
        other = userRepository.save(User.create("kakao-2", "다른유저", "other@test.com"));
    }

    @Test
    void findAllByReceiverId_성공() {
        // given
        notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_RECEIVED, "지원이 왔습니다", UUID.randomUUID()));
        notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_APPROVED, "승인되었습니다", UUID.randomUUID()));
        notificationRepository.save(Notification.create(other, NotificationType.APPLY_RECEIVED, "다른 유저 알림", UUID.randomUUID()));

        // when
        Page<Notification> result = notificationRepository.findAllByReceiverId(receiver.getId(), PageRequest.of(0, 20));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(n -> n.getReceiver().getId().equals(receiver.getId()));
    }

    @Test
    void findAllByReceiverId_알림없음_빈페이지() {
        // when
        Page<Notification> result = notificationRepository.findAllByReceiverId(receiver.getId(), PageRequest.of(0, 20));

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void countByReceiverIdAndIsReadFalse_읽지않은알림_카운트() {
        // given
        Notification read = notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_RECEIVED, "읽은 알림", UUID.randomUUID()));
        read.markAsRead();
        notificationRepository.save(read);
        notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_APPROVED, "안 읽은 알림", UUID.randomUUID()));

        // when
        long count = notificationRepository.countByReceiverIdAndIsReadFalse(receiver.getId());

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByReceiverIdAndIsReadFalse_모두읽음_0() {
        // given
        Notification n = notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_RECEIVED, "알림", UUID.randomUUID()));
        n.markAsRead();
        notificationRepository.save(n);

        // when
        long count = notificationRepository.countByReceiverIdAndIsReadFalse(receiver.getId());

        // then
        assertThat(count).isZero();
    }

    @Test
    void markAllAsRead_안읽은알림_모두읽음처리() {
        // given
        notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_RECEIVED, "알림1", UUID.randomUUID()));
        notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_APPROVED, "알림2", UUID.randomUUID()));

        // when
        notificationRepository.markAllAsRead(receiver.getId());

        // then
        long unreadCount = notificationRepository.countByReceiverIdAndIsReadFalse(receiver.getId());
        assertThat(unreadCount).isZero();
    }

    @Test
    void markAllAsRead_다른유저알림_영향없음() {
        // given
        notificationRepository.save(Notification.create(receiver, NotificationType.APPLY_RECEIVED, "수신자 알림", UUID.randomUUID()));
        notificationRepository.save(Notification.create(other, NotificationType.APPLY_RECEIVED, "다른유저 알림", UUID.randomUUID()));

        // when
        notificationRepository.markAllAsRead(receiver.getId());

        // then
        long otherUnreadCount = notificationRepository.countByReceiverIdAndIsReadFalse(other.getId());
        assertThat(otherUnreadCount).isEqualTo(1);
    }
}
