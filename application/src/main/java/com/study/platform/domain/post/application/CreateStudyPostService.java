package com.study.platform.domain.post.application;

import com.study.platform.domain.post.dto.request.StudyPostCreateRequest;
import com.study.platform.domain.post.dto.response.StudyPostResponse;
import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.post.usecase.CreateStudyPostUseCase;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.event.DomainEventPublisher;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.outbox.application.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateStudyPostService implements CreateStudyPostUseCase {

    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public StudyPostResponse execute(UUID userId, StudyPostCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        StudyPost post = studyPostRepository.save(
                StudyPost.create(user, request.title(), request.description(),
                        request.techStack(), request.maxMembers(), request.deadline()));
        Long outboxEventId = saveOutboxEvent(post.getId(), PostSyncOperationType.UPSERT);
        eventPublisher.publish(new PostSyncEvent(post.getId(), PostSyncOperationType.UPSERT, outboxEventId, 0));
        return StudyPostResponse.from(post);
    }

    private Long saveOutboxEvent(UUID postId, PostSyncOperationType operationType) {
        PostSyncEvent event = new PostSyncEvent(postId, operationType, null, 0);
        String payload = objectMapper.writeValueAsString(event);
        return outboxEventService.save(KafkaConstants.POST_SYNC_TOPIC, postId.toString(), payload);
    }
}
