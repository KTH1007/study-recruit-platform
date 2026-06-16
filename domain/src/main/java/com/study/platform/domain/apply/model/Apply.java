package com.study.platform.domain.apply.model;

import com.study.platform.domain.apply.event.ApplyApprovedEvent;
import com.study.platform.domain.apply.event.ApplyReceivedEvent;
import com.study.platform.domain.apply.event.ApplyRejectedEvent;
import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.entity.BaseTimeEntity;
import com.study.platform.global.event.DomainEventPublisher;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Apply {

    private UUID id;
    private StudyPost post;
    private User applicant;
    private String message;
    private ApplyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Apply(UUID id, StudyPost post, User applicant, String message, ApplyStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.post = post;
        this.applicant = applicant;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Apply create(StudyPost post, User applicant, String message, DomainEventPublisher publisher) {
        Apply apply = new Apply(
                UUID.randomUUID(), post, applicant, message,
                ApplyStatus.PENDING, LocalDateTime.now()
        );
        publisher.publish(new ApplyReceivedEvent(post.getId(), post.getAuthor().getId(), post.getTitle()));
        return apply;
    }

    // 영속성 계층에서 복원할 때 사용
    public static Apply reconstitute(UUID id, StudyPost post, User applicant,
                                     String message, ApplyStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Apply apply = new Apply(id, post, applicant, message, status, createdAt);
        apply.updatedAt = updatedAt;
        return apply;
    }

    public void approve(DomainEventPublisher publisher) {
        validatePending();
        this.status = ApplyStatus.APPROVED;
        publisher.publish(new ApplyApprovedEvent(post.getId(), applicant.getId(), post.getTitle()));
    }

    public void reject(DomainEventPublisher publisher) {
        validatePending();
        this.status = ApplyStatus.REJECTED;
        publisher.publish(new ApplyRejectedEvent(post.getId(), applicant.getId(), post.getTitle()));
    }

    private void validatePending() {
        if (this.status != ApplyStatus.PENDING) {
            throw new CustomException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }
    }
}
