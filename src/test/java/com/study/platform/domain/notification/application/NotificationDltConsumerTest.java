package com.study.platform.domain.notification.application;

import com.study.platform.domain.notification.dto.event.NotificationEvent;
import com.study.platform.domain.notification.model.FailedNotification;
import com.study.platform.domain.notification.model.FailedNotificationRepository;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.global.constant.KafkaConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationDltConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private FailedNotificationRepository failedNotificationRepository;

    @Mock
    private Acknowledgment ack;

    @InjectMocks
    private NotificationDltConsumer notificationDltConsumer;

    private UUID receiverId;
    private UUID targetId;
    private String payload;

    @BeforeEach
    void setUp() {
        receiverId = UUID.randomUUID();
        targetId = UUID.randomUUID();
        payload = "{}";
    }

    @Test
    void consume_재시도횟수_미만_메인토픽_재투입() throws Exception {
        // given
        NotificationEvent event = new NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", targetId, 1);
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        given(objectMapper.readValue(payload, NotificationEvent.class)).willReturn(event);
        given(objectMapper.writeValueAsString(any())).willReturn(payload);
        given(kafkaTemplate.send(eq(KafkaConstants.NOTIFICATION_TOPIC), any(String.class))).willReturn(future);

        // when
        notificationDltConsumer.consume(payload, ack, "처리 실패");

        // then
        then(kafkaTemplate).should().send(eq(KafkaConstants.NOTIFICATION_TOPIC), any(String.class));
        then(failedNotificationRepository).should(never()).save(any());
        then(ack).should().acknowledge();
    }

    @Test
    void consume_재시도횟수_초과_DB_영구저장() throws Exception {
        // given
        NotificationEvent event = new NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", targetId, KafkaConstants.MAX_DLT_RETRY);
        given(objectMapper.readValue(payload, NotificationEvent.class)).willReturn(event);

        // when
        notificationDltConsumer.consume(payload, ack, "처리 실패");

        // then
        then(failedNotificationRepository).should().save(any(FailedNotification.class));
        then(kafkaTemplate).should(never()).send(any(), any(String.class));
        then(ack).should().acknowledge();
    }

    @Test
    void consume_페이로드_파싱_실패_ack_처리() throws Exception {
        // given
        given(objectMapper.readValue(payload, NotificationEvent.class)).willThrow(new RuntimeException("파싱 실패"));

        // when
        notificationDltConsumer.consume(payload, ack, null);

        // then
        then(kafkaTemplate).should(never()).send(any(), any(String.class));
        then(failedNotificationRepository).should(never()).save(any());
        then(ack).should().acknowledge();
    }

    @Test
    void consume_재투입시_retryCount_증가() throws Exception {
        // given
        NotificationEvent event = new NotificationEvent(receiverId, NotificationType.APPLY_APPROVED, "승인됐습니다", targetId, 0);
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        given(objectMapper.readValue(payload, NotificationEvent.class)).willReturn(event);
        given(objectMapper.writeValueAsString(event.withRetry())).willReturn("{\"retryCount\":1}");
        given(kafkaTemplate.send(eq(KafkaConstants.NOTIFICATION_TOPIC), any(String.class))).willReturn(future);

        // when
        notificationDltConsumer.consume(payload, ack, null);

        // then
        then(kafkaTemplate).should().send(eq(KafkaConstants.NOTIFICATION_TOPIC), contains("retryCount"));
    }
}