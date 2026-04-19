package com.study.platform.domain.post.application;

import com.study.platform.domain.post.document.PostDocument;
import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
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

    @KafkaListener(topics = KafkaConstants.POST_SYNC_TOPIC, groupId = KafkaConstants.POST_SYNC_GROUP)
    public void consume(String payload, Acknowledgment ack) {
        PostSyncEvent event = objectMapper.readValue(payload, PostSyncEvent.class);

        if (event.operationType() == PostSyncOperationType.DELETE) {
            postSearchService.delete(event.postId().toString());
        } else {
            studyPostRepository.findByIdWithAuthor(event.postId())
                    .ifPresent(post -> postSearchService.index(PostDocument.from(post)));
        }

        ack.acknowledge();
        log.info("ES 동기화 완료 - postId: {}, type: {}", event.postId(), event.operationType());
    }
}
