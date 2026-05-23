package com.study.platform.global.outbox.application;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.domain.post.model.FailedPostSync;
import com.study.platform.domain.post.model.FailedPostSyncRepository;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.outbox.model.OutboxEvent;
import com.study.platform.global.outbox.model.OutboxEventRepository;
import com.study.platform.global.outbox.model.OutboxEventStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OutboxRetrySchedulerTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private OutboxEventService outboxEventService;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private FailedPostSyncRepository failedPostSyncRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxRetryScheduler outboxRetryScheduler;

    private OutboxEvent pendingOutbox(String topic, int retryCount) {
        OutboxEvent outbox = OutboxEvent.pending(topic, UUID.randomUUID().toString(), "{}");
        ReflectionTestUtils.setField(outbox, "retryCount", retryCount);
        return outbox;
    }

    @Test
    void retryPendingEvents_Kafka_성공_markSent_호출() throws Exception {
        // given
        OutboxEvent outbox = pendingOutbox(KafkaConstants.POST_SYNC_TOPIC, 0);
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.complete(mock(SendResult.class));
        given(outboxEventRepository.findAllByStatusAndCreatedAtBefore(eq(OutboxEventStatus.PENDING), any(LocalDateTime.class)))
                .willReturn(List.of(outbox));
        given(kafkaTemplate.send(anyString(), anyString(), anyString())).willReturn(future);

        // when
        outboxRetryScheduler.retryPendingEvents();

        // then
        then(outboxEventService).should().markSent(anyString(), eq(KafkaConstants.POST_SYNC_TOPIC));
    }

    @Test
    void retryPendingEvents_Kafka_실패_재시도횟수_증가() throws Exception {
        // given
        OutboxEvent outbox = pendingOutbox(KafkaConstants.POST_SYNC_TOPIC, 0);
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka 연결 실패"));
        given(outboxEventRepository.findAllByStatusAndCreatedAtBefore(eq(OutboxEventStatus.PENDING), any(LocalDateTime.class)))
                .willReturn(List.of(outbox));
        given(kafkaTemplate.send(anyString(), anyString(), anyString())).willReturn(future);

        // when
        outboxRetryScheduler.retryPendingEvents();

        // then
        then(outboxEventRepository).should().save(outbox);
        then(outboxEventService).should(never()).markSent(anyString(), anyString());
    }

    @Test
    void retryPendingEvents_최대재시도_초과_PostSync_FailedPostSync_저장() throws Exception {
        // given
        OutboxEvent outbox = pendingOutbox(KafkaConstants.POST_SYNC_TOPIC, 3);
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka 연결 실패"));
        given(outboxEventRepository.findAllByStatusAndCreatedAtBefore(eq(OutboxEventStatus.PENDING), any(LocalDateTime.class)))
                .willReturn(List.of(outbox));
        given(kafkaTemplate.send(anyString(), anyString(), anyString())).willReturn(future);
        given(objectMapper.readValue(anyString(), eq(PostSyncEvent.class))).willReturn(
                new PostSyncEvent(UUID.randomUUID(), PostSyncOperationType.UPSERT, 3));

        // when
        outboxRetryScheduler.retryPendingEvents();

        // then
        then(failedPostSyncRepository).should().save(any(FailedPostSync.class));
        then(outboxEventService).should().markSent(anyString(), eq(KafkaConstants.POST_SYNC_TOPIC));
    }

    @Test
    void retryPendingEvents_최대재시도_초과_Notification_FailedPostSync_미저장() throws Exception {
        // given
        OutboxEvent outbox = pendingOutbox(KafkaConstants.NOTIFICATION_TOPIC, 3);
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka 연결 실패"));
        given(outboxEventRepository.findAllByStatusAndCreatedAtBefore(eq(OutboxEventStatus.PENDING), any(LocalDateTime.class)))
                .willReturn(List.of(outbox));
        given(kafkaTemplate.send(anyString(), anyString(), anyString())).willReturn(future);

        // when
        outboxRetryScheduler.retryPendingEvents();

        // then
        then(failedPostSyncRepository).should(never()).save(any());
        then(outboxEventService).should().markSent(anyString(), eq(KafkaConstants.NOTIFICATION_TOPIC));
    }
}