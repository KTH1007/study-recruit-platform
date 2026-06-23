package com.study.platform.domain.apply.infrastructure

import com.study.platform.domain.apply.model.ApplyStatus
import com.study.platform.domain.post.model.StudyPost
import com.study.platform.domain.user.model.User
import com.study.platform.global.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "applies",
    uniqueConstraints = [UniqueConstraint(columnNames = ["post_id", "applicant_id"])],
    indexes = [
        Index(name = "idx_apply_post_status_created", columnList = "post_id, status, created_at"),
        Index(name = "idx_apply_applicant_id", columnList = "applicant_id")
    ]
)
class ApplyJpaEntity(
    @Id
    @Column(columnDefinition = "BINARY(16)")
    val id: UUID,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: StudyPost,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    val applicant: User,

    @Column(columnDefinition = "TEXT")
    val message: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ApplyStatus
) : BaseTimeEntity() {

    fun updateStatus(status: ApplyStatus) {
        this.status = status
    }

    fun restoreTimestamps(createdAt: LocalDateTime, updatedAt: LocalDateTime?) {
        initTimestamps(createdAt, updatedAt)
    }
}
