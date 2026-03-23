package com.study.platform.domain.comment.model;

import com.study.platform.domain.post.model.StudyPost;
import com.study.platform.domain.user.model.User;
import com.study.platform.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "comment")
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

    public static Comment create(StudyPost post, User author, String content) {
        return Comment.builder()
                .post(post)
                .author(author)
                .content(content)
                .build();
    }

    public void update(String content) {
        this.content = content;
    }

    public boolean isAuthor(UUID userId) {
        return this.author.getId().equals(userId);
    }
}
