package com.study.platform.domain.post.application;

import com.study.platform.domain.post.dto.request.StudyPostCreateRequest;
import com.study.platform.domain.post.dto.request.StudyPostUpdateRequest;
import com.study.platform.domain.post.dto.response.StudyPostResponse;
import com.study.platform.domain.post.dto.response.StudyPostSummaryResponse;
import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.post.model.StudyPostRepository;
import com.study.platform.domain.post.model.StudyPostStatus;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.constant.CacheConstants;
import com.study.platform.global.constant.KafkaConstants;
import com.study.platform.global.event.DomainEventPublisher;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.outbox.application.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyPostService {

    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;

    public Page<StudyPostSummaryResponse> findPosts(String techStack, StudyPostStatus status, Pageable pageable) {
        return studyPostRepository.findAllWithFilter(techStack, status, pageable)
                .map(StudyPostSummaryResponse::from);
    }

    @Cacheable(cacheNames = CacheConstants.POST_CACHE, key = "#postId")
    public StudyPostResponse findPost(UUID postId) {
        return StudyPostResponse.from(getPostWithAuthor(postId));
    }

    @Transactional
    public StudyPostResponse createPost(UUID userId, StudyPostCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        StudyPost post = studyPostRepository.save(
                StudyPost.create(user, request.title(), request.description(),
                        request.techStack(), request.maxMembers(), request.deadline()));
        Long outboxEventId = saveOutboxEvent(post.getId(), PostSyncOperationType.UPSERT);
        eventPublisher.publish(new PostSyncEvent(post.getId(), PostSyncOperationType.UPSERT, outboxEventId, 0));
        return StudyPostResponse.from(post);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConstants.POST_CACHE, key = "#postId")
    public StudyPostResponse updatePost(UUID userId, UUID postId, StudyPostUpdateRequest request) {
        StudyPost post = getPostWithAuthor(postId);
        post.validateAuthor(userId);
        post.update(request.title(), request.description(), request.techStack(),
                request.maxMembers(), request.deadline());
        Long outboxEventId = saveOutboxEvent(postId, PostSyncOperationType.UPSERT);
        eventPublisher.publish(new PostSyncEvent(postId, PostSyncOperationType.UPSERT, outboxEventId, 0));
        return StudyPostResponse.from(post);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConstants.POST_CACHE, key = "#postId")
    public void deletePost(UUID userId, UUID postId) {
        StudyPost post = getPostWithAuthor(postId);
        post.validateAuthor(userId);
        studyPostRepository.delete(post);
        Long outboxEventId = saveOutboxEvent(postId, PostSyncOperationType.DELETE);
        eventPublisher.publish(new PostSyncEvent(postId, PostSyncOperationType.DELETE, outboxEventId, 0));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConstants.POST_CACHE, key = "#postId")
    public StudyPostResponse closePost(UUID userId, UUID postId) {
        StudyPost post = getPostWithAuthor(postId);
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

    private StudyPost getPostWithAuthor(UUID postId) {
        return studyPostRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }
}
