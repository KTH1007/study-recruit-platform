package com.study.platform.global.outbox.application;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.model.FailedPostSync;
import com.study.platform.domain.post.model.FailedPostSyncRepository;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.kafka.KafkaMessagePublisher;
import com.study.platform.global.outbox.model.OutboxEvent;
import com.study.platform.global.outbox.model.OutboxEventRepository;
import com.study.platform.global.outbox.model.OutboxEventStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRetryScheduler {

    private static final int MAX_OUTBOX_RETRY = 3;

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventService outboxEventService;
    private final KafkaMessagePublisher kafkaMessagePublisher;
    private final FailedPostSyncRepository failedPostSyncRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 30_000)
    public void retryPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findAllByStatusAndCreatedAtBefore(OutboxEventStatus.PENDING, LocalDateTime.now().minusSeconds(30));

        for (OutboxEvent outbox : pendingEvents) {
            retry(outbox);
        }
    }

    private void retry(OutboxEvent outbox) {
        kafkaMessagePublisher.publish(outbox.getTopic(), outbox.getMessageKey(), outbox.getPayload())
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        outboxEventService.markSent(outbox.getId());
                        log.info("Outbox 재발행 성공 - id: {}, topic: {}", outbox.getId(), outbox.getTopic());
                    } else {
                        log.warn("Outbox 재발행 실패 - id: {}, topic: {}", outbox.getId(), outbox.getTopic(), ex);
                        handleRetryFailure(outbox, ex);
                    }
                });
    }

    private void handleRetryFailure(OutboxEvent outbox, Throwable e) {
        if (outbox.getRetryCount() >= MAX_OUTBOX_RETRY) {
            log.error("Outbox 최대 재시도 초과 - id: {}, topic: {}", outbox.getId(), outbox.getTopic());
            if (outbox.getTopic().equals(KafkaConstants.POST_SYNC_TOPIC)) {
                PostSyncEvent event = objectMapper.readValue(outbox.getPayload(), PostSyncEvent.class);
                failedPostSyncRepository.save(FailedPostSync.from(event, e.getMessage()));
            }
            outboxEventService.markFailedPermanently(outbox.getId());
        } else {
            outbox.incrementRetryCount();
            outboxEventRepository.save(outbox);
        }
    }
}
