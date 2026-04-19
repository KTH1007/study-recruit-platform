package com.study.platform.domain.post.application;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.global.constant.KafkaConstants;
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

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostSyncEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(KafkaConstants.POST_SYNC_TOPIC, event.postId().toString(), payload);
        log.info("ES 동기화 이벤트 발행 - postId: {}, type: {}", event.postId(), event.operationType());
    }
}
