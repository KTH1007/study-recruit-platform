package com.study.platform.domain.post.model;

import com.study.platform.domain.user.model.User;
import com.study.platform.global.entity.BaseTimeEntity;
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
                @Index(name = "idx_study_post_author_id", columnList = "author_id")
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

    @Column(length = 100, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(length = 255)
    private String techStack;

    @Column(nullable = false)
    private int maxMembers;

    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyPostStatus status;

    @Builder
    private StudyPost(User author, String title, String description,
                      String techStack, int maxMembers, LocalDateTime deadline) {
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
                .title(title)
                .description(description)
                .techStack(techStack)
                .maxMembers(maxMembers)
                .deadline(deadline)
                .build();
    }

    public void update(String title, String description, String techStack,
                       int maxMembers, LocalDateTime deadline) {
        this.title = title;
        this.description = description;
        this.techStack = techStack;
        this.maxMembers = maxMembers;
        this.deadline = deadline;
    }

    public void close() {
        this.status = StudyPostStatus.CLOSED;
    }

    public void markFull() {
        this.status = StudyPostStatus.FULL;
    }

    public boolean isAuthor(UUID userId) {
        return this.author.getId().equals(userId);
    }

    public boolean isOpen() {
        return this.status == StudyPostStatus.OPEN;
    }
}
