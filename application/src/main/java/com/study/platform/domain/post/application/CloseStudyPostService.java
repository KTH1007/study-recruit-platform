package com.study.platform.domain.post.application;

import com.study.platform.domain.post.dto.response.StudyPostResponse;
import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.post.usecase.CloseStudyPostUseCase;
import com.study.platform.global.constant.CacheConstants;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.event.DomainEventPublisher;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.outbox.application.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloseStudyPostService implements CloseStudyPostUseCase {

    private final StudyPostRepository studyPostRepository;
    private final DomainEventPublisher eventPublisher;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConstants.POST_CACHE, key = "#postId")
    public StudyPostResponse execute(UUID userId, UUID postId) {
        StudyPost post = studyPostRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        post.validateAuthor(userId);
        post.close();
        Long outboxEventId = saveOutboxEvent(postId, PostSyncOperationType.UPSERT);
        eventPublisher.publish(new PostSyncEvent(postId, PostSyncOperationType.UPSERT, outboxEventId, 0));
        return StudyPostResponse.from(post);
    }

    private Long saveOutboxEvent(UUID postId, PostSyncOperationType operationType) {
        PostSyncEvent event = new PostSyncEvent(postId, operationType, null, 0);
        String payload = objectMapper.writeValueAsString(event);
        return outboxEventService.save(KafkaConstants.POST_SYNC_TOPIC, postId.toString(), payload);
    }
}
