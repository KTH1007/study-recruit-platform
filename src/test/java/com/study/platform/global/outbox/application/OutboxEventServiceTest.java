package com.study.platform.global.outbox.application;

import com.study.platform.global.outbox.model.OutboxEvent;
import com.study.platform.global.outbox.model.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class OutboxEventServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private OutboxEventService outboxEventService;

    @Test
    void save_정상_PENDING_저장() {
        // when
        outboxEventService.save("post-sync", "postId-123", "{\"postId\":\"123\"}");

        // then
        then(outboxEventRepository).should().save(any(OutboxEvent.class));
    }

    @Test
    void markSent_정상_SENT_업데이트() {
        // when
        outboxEventService.markSent("postId-123", "post-sync");

        // then
        then(outboxEventRepository).should().markSentByMessageKeyAndTopic(eq("postId-123"), eq("post-sync"));
    }
}
