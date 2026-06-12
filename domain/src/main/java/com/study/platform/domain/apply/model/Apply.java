package com.study.platform.domain.apply.model;

import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.entity.BaseTimeEntity;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "applies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "applicant_id"}),
        indexes = {
                @Index(name = "idx_apply_post_status_created", columnList = "post_id, status, created_at"),
                @Index(name = "idx_apply_applicant_id", columnList = "applicant_id")
        }
)
public class Apply extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private StudyPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplyStatus status;

    @Builder
    private Apply(StudyPost post, User applicant, String message) {
        this.post = post;
        this.applicant = applicant;
        this.message = message;
        this.status = ApplyStatus.PENDING;
    }

    public static Apply create(StudyPost post, User applicant, String message) {
        return Apply.builder()
                .post(post)
                .applicant(applicant)
                .message(message)
                .build();
    }

    public void approve() {
        validatePending();
        this.status = ApplyStatus.APPROVED;
    }

    public void reject() {
        validatePending();
        this.status = ApplyStatus.REJECTED;
    }

    private void validatePending() {
        if (this.status != ApplyStatus.PENDING) {
            throw new CustomException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }
    }
}
