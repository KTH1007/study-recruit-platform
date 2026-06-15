package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.model.NotificationEvent;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.support.fake.FakeFailedNotificationRepository;
import com.study.platform.support.fake.FakeNotificationRepository;
import com.study.platform.support.fake.FakeRedisMessagePublisher;
import com.study.platform.support.fake.FakeUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NotificationKafkaConsumerTest {

    @Mock
    private Acknowledgment ack;

    private FakeUserRepository userRepository;
    private FakeNotificationRepository notificationRepository;
    private FakeRedisMessagePublisher redisMessagePublisher;
    private FakeFailedNotificationRepository failedNotificationRepository;
    private NotificationKafkaConsumer notificationKafkaConsumer;
    private ObjectMapper objectMapper;

    private UUID receiverId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userRepository = new FakeUserRepository();
        notificationRepository = new FakeNotificationRepository();
        redisMessagePublisher = new FakeRedisMessagePublisher();
        failedNotificationRepository = new FakeFailedNotificationRepository();
        notificationKafkaConsumer = new NotificationKafkaConsumer(
                objectMapper, userRepository, notificationRepository, redisMessagePublisher, failedNotificationRepository);
        receiverId = UUID.randomUUID();
    }

    @Test
    void consume_최대재시도초과_정상흐름_건너뛰고_DB저장() throws Exception {
        // given
        NotificationEvent event = new NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", UUID.randomUUID(), KafkaConstants.MAX_DLT_RETRY);

        // when
        notificationKafkaConsumer.consume(objectMapper.writeValueAsString(event), ack);

        // then
        assertThat(failedNotificationRepository.getSaved()).hasSize(1);
        assertThat(userRepository.findById(receiverId)).isEmpty();
        then(ack).should().acknowledge();
    }

    @Test
    void consume_정상처리() throws Exception {
        // given
        NotificationEvent event = new NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", UUID.randomUUID(), 0);
        User receiver = User.create("kakao1", "수신자", "receiver@test.com");
        ReflectionTestUtils.setField(receiver, "id", receiverId);
        userRepository.save(receiver);

        // when
        notificationKafkaConsumer.consume(objectMapper.writeValueAsString(event), ack);

        // then
        assertThat(notificationRepository.findAllByReceiverId(receiverId)).isNotEmpty();
        assertThat(failedNotificationRepository.getSaved()).isEmpty();
        assertThat(redisMessagePublisher.wasPublishedTo("notification")).isTrue();
        then(ack).should().acknowledge();
    }
}
