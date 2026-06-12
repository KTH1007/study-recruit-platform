package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.model.FailedNotification;
import com.study.platform.domain.notification.model.NotificationEvent;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.support.fake.FakeFailedNotificationRepository;
import com.study.platform.support.fake.FakeKafkaMessagePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NotificationDltConsumerTest {

    @Mock
    private Acknowledgment ack;

    private FakeKafkaMessagePublisher kafkaPublisher;
    private FakeFailedNotificationRepository failedNotificationRepository;
    private NotificationDltConsumer notificationDltConsumer;
    private ObjectMapper objectMapper;

    private UUID receiverId;
    private UUID targetId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        kafkaPublisher = new FakeKafkaMessagePublisher();
        failedNotificationRepository = new FakeFailedNotificationRepository();
        notificationDltConsumer = new NotificationDltConsumer(objectMapper, kafkaPublisher, failedNotificationRepository);
        receiverId = UUID.randomUUID();
        targetId = UUID.randomUUID();
    }

    @Test
    void consume_재시도횟수_미만_메인토픽_재투입() throws Exception {
        // given
        NotificationEvent event = new NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", targetId, 1);

        // when
        notificationDltConsumer.consume(objectMapper.writeValueAsString(event), ack, "처리 실패");

        // then
        assertThat(kafkaPublisher.wasPublishedTo(KafkaConstants.NOTIFICATION_TOPIC)).isTrue();
        assertThat(failedNotificationRepository.getSaved()).isEmpty();
        then(ack).should().acknowledge();
    }

    @Test
    void consume_재시도횟수_초과_DB_영구저장() throws Exception {
        // given
        NotificationEvent event = new NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", targetId, KafkaConstants.MAX_DLT_RETRY);

        // when
        notificationDltConsumer.consume(objectMapper.writeValueAsString(event), ack, "처리 실패");

        // then
        assertThat(failedNotificationRepository.getSaved()).hasSize(1);
        assertThat(failedNotificationRepository.getSaved().get(0)).isInstanceOf(FailedNotification.class);
        assertThat(kafkaPublisher.getPublishedTopics()).isEmpty();
        then(ack).should().acknowledge();
    }

    @Test
    void consume_페이로드_파싱_실패_ack_처리() {
        // when
        notificationDltConsumer.consume("invalid-json", ack, null);

        // then
        assertThat(kafkaPublisher.getPublishedTopics()).isEmpty();
        assertThat(failedNotificationRepository.getSaved()).isEmpty();
        then(ack).should().acknowledge();
    }

    @Test
    void consume_재투입시_retryCount_증가() throws Exception {
        // given
        NotificationEvent event = new NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", targetId, 0);

        // when
        notificationDltConsumer.consume(objectMapper.writeValueAsString(event), ack, null);

        // then
        assertThat(kafkaPublisher.wasPublishedTo(KafkaConstants.NOTIFICATION_TOPIC)).isTrue();
        assertThat(kafkaPublisher.getPublished().get(0).payload()).contains("retryCount");
    }
}
