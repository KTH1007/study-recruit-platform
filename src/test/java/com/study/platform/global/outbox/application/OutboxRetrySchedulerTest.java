package com.study.platform.global.outbox.application;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.outbox.model.OutboxEvent;
import com.study.platform.global.outbox.model.OutboxEventStatus;
import com.study.platform.support.fake.FakeFailedPostSyncRepository;
import com.study.platform.support.fake.FakeKafkaMessagePublisher;
import com.study.platform.support.fake.FakeOutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OutboxRetrySchedulerTest {

    @Mock
    private OutboxEventService outboxEventService;

    private FakeOutboxEventRepository outboxEventRepository;
    private FakeKafkaMessagePublisher kafkaPublisher;
    private FakeFailedPostSyncRepository failedPostSyncRepository;
    private OutboxRetryScheduler outboxRetryScheduler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxEventRepository = new FakeOutboxEventRepository();
        kafkaPublisher = new FakeKafkaMessagePublisher();
        failedPostSyncRepository = new FakeFailedPostSyncRepository();
        outboxRetryScheduler = new OutboxRetryScheduler(
                outboxEventRepository, outboxEventService, kafkaPublisher, failedPostSyncRepository, objectMapper);
    }

    private OutboxEvent pendingOutbox(String topic, int retryCount) {
        OutboxEvent outbox = OutboxEvent.pending(topic, UUID.randomUUID().toString(), "{}");
        ReflectionTestUtils.setField(outbox, "id", 1L);
        ReflectionTestUtils.setField(outbox, "retryCount", retryCount);
        outboxEventRepository.save(outbox);
        return outbox;
    }

    @Test
    void retryPendingEvents_Kafka_성공_markSent_호출() {
        // given
        OutboxEvent outbox = pendingOutbox(KafkaConstants.POST_SYNC_TOPIC, 0);

        // when
        outboxRetryScheduler.retryPendingEvents();

        // then
        then(outboxEventService).should().markSent(outbox.getId());
    }

    @Test
    void retryPendingEvents_Kafka_실패_재시도횟수_증가() {
        // given
        OutboxEvent outbox = pendingOutbox(KafkaConstants.POST_SYNC_TOPIC, 0);
        kafkaPublisher.willFail();

        // when
        outboxRetryScheduler.retryPendingEvents();

        // then
        assertThat(outbox.getRetryCount()).isEqualTo(1);
        then(outboxEventService).should(never()).markSent(anyLong());
    }

    @Test
    void retryPendingEvents_최대재시도_초과_PostSync_FailedPostSync_저장() throws Exception {
        // given
        PostSyncEvent syncEvent = new PostSyncEvent(UUID.randomUUID(), PostSyncOperationType.UPSERT, null, 3);
        String payload = objectMapper.writeValueAsString(syncEvent);
        OutboxEvent outbox = OutboxEvent.pending(KafkaConstants.POST_SYNC_TOPIC, UUID.randomUUID().toString(), payload);
        ReflectionTestUtils.setField(outbox, "id", 1L);
        ReflectionTestUtils.setField(outbox, "retryCount", 3);
        outboxEventRepository.save(outbox);
        kafkaPublisher.willFail();

        // when
        outboxRetryScheduler.retryPendingEvents();

        // then
        assertThat(failedPostSyncRepository.getSaved()).hasSize(1);
        then(outboxEventService).should().markFailedPermanently(outbox.getId());
    }

    @Test
    void retryPendingEvents_최대재시도_초과_Notification_FailedPostSync_미저장() {
        // given
        pendingOutbox(KafkaConstants.NOTIFICATION_TOPIC, 3);
        kafkaPublisher.willFail();

        // when
        outboxRetryScheduler.retryPendingEvents();

        // then
        assertThat(failedPostSyncRepository.getSaved()).isEmpty();
        then(outboxEventService).should().markFailedPermanently(anyLong());
    }
}
