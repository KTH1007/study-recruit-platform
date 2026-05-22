package com.study.platform.domain.post.model;

import com.study.platform.domain.post.event.PostSyncEvent;
import com.study.platform.domain.post.event.PostSyncOperationType;
import com.study.platform.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "failed_post_syncs")
public class FailedPostSync extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, columnDefinition = "BINARY(16)")
    private UUID postId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PostSyncOperationType operationType;

    @Column(columnDefinition = "text")
    private String failureReason;

    public static FailedPostSync from(PostSyncEvent event, String failureReason) {
        FailedPostSync entity = new FailedPostSync();
        entity.postId = event.postId();
        entity.operationType = event.operationType();
        entity.failureReason = failureReason;
        return entity;
    }
}