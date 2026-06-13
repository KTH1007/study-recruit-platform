package com.study.platform.domain.post.model;

import com.study.platform.domain.user.model.User;
import com.study.platform.global.entity.BaseTimeEntity;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.model.TechStack;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "study_posts",
        indexes = {
                @Index(name = "idx_study_post_status_deadline", columnList = "status, deadline"),
                @Index(name = "idx_study_post_author_created", columnList = "author_id, created_at")
        }
)
public class StudyPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Getter(AccessLevel.NONE)
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "title", length = 100, nullable = false))
    private PostTitle title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Getter(AccessLevel.NONE)
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "tech_stack", length = 255))
    private TechStack techStack;

    @Getter(AccessLevel.NONE)
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "max_members", nullable = false))
    private MaxMembers maxMembers;

    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyPostStatus status;

    @Builder
    private StudyPost(User author, PostTitle title, String description,
                      TechStack techStack, MaxMembers maxMembers, LocalDateTime deadline) {
        this.author = author;
        this.title = title;
        this.description = description;
        this.techStack = techStack;
        this.maxMembers = maxMembers;
        this.deadline = deadline;
        this.status = StudyPostStatus.OPEN;
    }

    public static StudyPost create(User author, String title, String description,
                                   String techStack, int maxMembers, LocalDateTime deadline) {
        return StudyPost.builder()
                .author(author)
                .title(new PostTitle(title))
                .description(description)
                .techStack(TechStack.of(techStack))
                .maxMembers(new MaxMembers(maxMembers))
                .deadline(deadline)
                .build();
    }

    public String getTitle() {
        return title.value();
    }

    public String getTechStack() {
        return techStack != null ? techStack.value() : null;
    }

    public int getMaxMembers() {
        return maxMembers.value();
    }

    public void update(String title, String description, String techStack,
                       int maxMembers, LocalDateTime deadline) {
        this.title = new PostTitle(title);
        this.description = description;
        this.techStack = TechStack.of(techStack);
        this.maxMembers = new MaxMembers(maxMembers);
        this.deadline = deadline;
    }

    public void close() {
        this.status = StudyPostStatus.CLOSED;
    }

    public void markFull() {
        this.status = StudyPostStatus.FULL;
    }

    public void markFullIfNeeded(long approvedCount) {
        if (approvedCount >= maxMembers.value()) {
            this.status = StudyPostStatus.FULL;
        }
    }

    public boolean isAuthor(UUID userId) {
        return this.author.getId().equals(userId);
    }

    public boolean isOpen() {
        return this.status == StudyPostStatus.OPEN;
    }

    public boolean isFull(long approvedCount) {
        return approvedCount >= maxMembers.value();
    }

    public void validateOpen() {
        if (!isOpen()) {
            throw new CustomException(ErrorCode.POST_CLOSED);
        }
    }

    public void validateAuthor(UUID userId) {
        if (!isAuthor(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    public void validateNotAuthor(UUID userId) {
        if (isAuthor(userId)) {
            throw new CustomException(ErrorCode.CANNOT_APPLY_OWN_POST);
        }
    }
}
