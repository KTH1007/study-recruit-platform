package com.study.platform.domain.post.application;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.outbox.application.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostSyncKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxEventService outboxEventService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostSyncEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(KafkaConstants.POST_SYNC_TOPIC, event.postId().toString(), payload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        outboxEventService.markSent(event.postId().toString(), KafkaConstants.POST_SYNC_TOPIC);
                        log.info("ES 동기화 이벤트 발행 성공 - postId: {}, type: {}", event.postId(), event.operationType());
                    } else {
                        log.warn("ES 동기화 이벤트 발행 실패 - outbox 스케줄러가 재시도 예정. postId: {}, type: {}", event.postId(), event.operationType(), ex);
                    }
                });
    }
}
