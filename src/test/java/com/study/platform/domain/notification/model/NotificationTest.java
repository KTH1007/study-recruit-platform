package com.study.platform.domain.notification.model;

import com.study.platform.domain.user.model.User;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;


class NotificationTest {

    private User receiver;
    private Notification notification;

    @BeforeEach
    void setUp() {
        receiver = User.create("kakao-1", "수신자", "receiver@test.com");
        ReflectionTestUtils.setField(receiver, "id", UUID.randomUUID());

        notification = Notification.create(receiver, NotificationType.APPLY_RECEIVED, "지원이 도착했습니다.", UUID.randomUUID());
    }

    @Test
    void validateReceiver_본인_예외없음() {
        assertThatNoException().isThrownBy(() -> notification.validateReceiver(receiver.getId()));
    }

    @Test
    void validateReceiver_본인아님_예외발생() {
        assertThatThrownBy(() -> notification.validateReceiver(UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));
    }
}