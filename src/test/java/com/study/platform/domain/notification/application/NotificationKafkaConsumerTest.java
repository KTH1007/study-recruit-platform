package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.dto.event.NotificationEvent;
import com.study.platform.domain.notification.model.FailedNotification;
import com.study.platform.domain.notification.model.FailedNotificationRepository;
import com.study.platform.domain.notification.model.NotificationRepository;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.constant.KafkaConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.support.Acknowledgment;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationKafkaConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private FailedNotificationRepository failedNotificationRepository;

    @Mock
    private Acknowledgment ack;

    @InjectMocks
    private NotificationKafkaConsumer notificationKafkaConsumer;

    private UUID receiverId;
    private String payload;

    @BeforeEach
    void setUp() {
        receiverId = UUID.randomUUID();
        payload = "{}";
    }

    @Test
    void consume_최대재시도초과_정상흐름_건너뛰고_DB저장() throws Exception {
        // given
        NotificationEvent event = new NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", UUID.randomUUID(), KafkaConstants.MAX_DLT_RETRY);
        given(objectMapper.readValue(payload, NotificationEvent.class)).willReturn(event);

        // when
        notificationKafkaConsumer.consume(payload, ack);

        // then
        then(failedNotificationRepository).should().save(any(FailedNotification.class));
        then(userRepository).should(never()).findById(any());
        then(notificationRepository).should(never()).save(any());
        then(ack).should().acknowledge();
    }

    @Test
    void consume_정상처리() throws Exception {
        // given
        NotificationEvent event = new NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", UUID.randomUUID(), 0);
        User receiver = User.create("kakao1", "수신자", "receiver@test.com");
        given(objectMapper.readValue(payload, NotificationEvent.class)).willReturn(event);
        given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));
        given(objectMapper.writeValueAsString(any())).willReturn("{}");

        // when
        notificationKafkaConsumer.consume(payload, ack);

        // then
        then(notificationRepository).should().save(any());
        then(failedNotificationRepository).should(never()).save(any());
        then(ack).should().acknowledge();
    }
}