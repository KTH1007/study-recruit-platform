package com.study.platform.domain.post.application;

import com.study.platform.global.constant.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostSyncDltConsumer {

    @KafkaListener(topics = KafkaConstants.POST_SYNC_DLT_TOPIC, groupId = KafkaConstants.POST_SYNC_DLT_GROUP)
    public void consume(String payload, Acknowledgment ack,
                        @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
        log.error("DLQ 수신 - payload: {}, 원인: {}", payload, exceptionMessage);
        ack.acknowledge();
    }
}
