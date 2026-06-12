package com.study.platform.global.outbox.application;

import com.study.platform.global.outbox.model.OutboxEvent;
import com.study.platform.global.outbox.model.OutboxEventStatus;
import com.study.platform.support.fake.FakeOutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventServiceTest {

    private FakeOutboxEventRepository outboxEventRepository;
    private OutboxEventService outboxEventService;

    @BeforeEach
    void setUp() {
        outboxEventRepository = new FakeOutboxEventRepository();
        outboxEventService = new OutboxEventService(outboxEventRepository);
    }

    @Test
    void save_정상_PENDING_저장() {
        // when
        outboxEventService.save("post-sync", "postId-123", "{\"postId\":\"123\"}");

        // then
        List<OutboxEvent> events = outboxEventRepository.findAllByStatusAndCreatedAtBefore(
                OutboxEventStatus.PENDING, LocalDateTime.MAX);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTopic()).isEqualTo("post-sync");
    }

    @Test
    void markSent_정상_SENT_업데이트() {
        // given
        OutboxEvent event = OutboxEvent.pending("post-sync", "postId-123", "{}");
        ReflectionTestUtils.setField(event, "id", 1L);
        outboxEventRepository.save(event);

        // when
        outboxEventService.markSent(1L);

        // then
        List<OutboxEvent> sentEvents = outboxEventRepository.findAllByStatusAndCreatedAtBefore(
                OutboxEventStatus.SENT, LocalDateTime.MAX);
        assertThat(sentEvents).hasSize(1);
    }
}
