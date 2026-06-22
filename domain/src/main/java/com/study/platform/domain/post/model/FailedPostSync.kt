package com.study.platform.domain.post.model

import com.study.platform.domain.post.event.PostSyncEvent
import com.study.platform.domain.post.event.PostSyncOperationType
import com.study.platform.global.entity.BaseTimeEntity
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "failed_post_syncs")
class FailedPostSync : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    var id: UUID? = null

    @Column(nullable = false, columnDefinition = "BINARY(16)")
    var postId: UUID? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var operationType: PostSyncOperationType? = null

    @Column(columnDefinition = "text")
    var failureReason: String? = null

    companion object {
        fun from(event: PostSyncEvent, failureReason: String): FailedPostSync =
            FailedPostSync().also {
                it.postId = event.postId
                it.operationType = event.operationType
                it.failureReason = failureReason
            }
    }
}
