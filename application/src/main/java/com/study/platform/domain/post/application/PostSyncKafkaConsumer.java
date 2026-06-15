package com.study.platform.domain.post.application;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.domain.post.model.FailedPostSync;
import com.study.platform.domain.post.model.FailedPostSyncRepository;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.global.constant.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostSyncKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final StudyPostRepository studyPostRepository;
    private final PostSearchService postSearchService;
    private final FailedPostSyncRepository failedPostSyncRepository;

    @KafkaListener(topics = KafkaConstants.POST_SYNC_TOPIC, groupId = KafkaConstants.POST_SYNC_GROUP)
    public void consume(String payload, Acknowledgment ack) {
        PostSyncEvent event = objectMapper.readValue(payload, PostSyncEvent.class);

        if (event.retryCount() >= KafkaConstants.MAX_DLT_RETRY) {
            failedPostSyncRepository.save(FailedPostSync.from(event, "메인 Consumer 최종 실패"));
            ack.acknowledge();
            return;
        }

        if (event.operationType() == PostSyncOperationType.DELETE) {
            postSearchService.delete(event.postId().toString());
        } else {
            studyPostRepository.findByIdWithAuthor(event.postId())
                    .ifPresent(postSearchService::index);
        }

        ack.acknowledge();
        log.info("ES 동기화 완료 - postId: {}, type: {}", event.postId(), event.operationType());
    }
}
