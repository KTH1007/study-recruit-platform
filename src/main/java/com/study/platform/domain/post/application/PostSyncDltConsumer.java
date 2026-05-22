package com.study.platform.domain.post.application;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.model.FailedPostSync;
import com.study.platform.domain.post.model.FailedPostSyncRepository;
import com.study.platform.global.constant.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostSyncDltConsumer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final FailedPostSyncRepository failedPostSyncRepository;

    @KafkaListener(topics = KafkaConstants.POST_SYNC_DLT_TOPIC, groupId = KafkaConstants.POST_SYNC_DLT_GROUP)
    public void consume(String payload, Acknowledgment ack,
                        @Header(name = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exceptionMessage) {
        PostSyncEvent event = objectMapper.readValue(payload, PostSyncEvent.class);
        log.error("DLT 수신 - postId={}, type={}, retryCount={}, 원인={}",
                event.postId(), event.operationType(), event.retryCount(), exceptionMessage);

        if (event.retryCount() < KafkaConstants.MAX_DLT_RETRY) {
            kafkaTemplate.send(KafkaConstants.POST_SYNC_TOPIC, objectMapper.writeValueAsString(event.withRetry()));
            log.info("post-sync 토픽 재투입 - retryCount={}", event.retryCount() + 1);
        } else {
            failedPostSyncRepository.save(FailedPostSync.from(event, exceptionMessage));
            log.error("최대 재시도 초과 - DB 영구 저장. postId={}", event.postId());
        }

        ack.acknowledge();
    }
}