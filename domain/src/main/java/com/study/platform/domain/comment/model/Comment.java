package com.study.platform.domain.comment.model;

import com.study.platform.domain.comment.event.CommentCreatedEvent;
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

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(name = "idx_comment_post_created", columnList = "post_id, created_at")
        }
)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private StudyPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder
    private Comment(StudyPost post, User author, String content) {
        this.post = post;
        this.author = author;
        this.content = content;
    }

    public static Comment create(StudyPost post, User author, String content, DomainEventPublisher publisher) {
        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(content)
                .build();
        if (post != null && !post.isAuthor(author.getId())) {
            publisher.publish(new CommentCreatedEvent(
                    post.getId(), post.getAuthor().getId(), author.getId(), post.getTitle()));
        }
        return comment;
    }

    public void update(String content) {
        this.content = content;
    }

    public boolean isAuthor(UUID userId) {
        return this.author.getId().equals(userId);
    }

    public void validateAuthor(UUID userId) {
        if (!isAuthor(userId)) {
            throw new CustomException(ErrorCode.NOT_COMMENT_AUTHOR);
        }
    }
}
