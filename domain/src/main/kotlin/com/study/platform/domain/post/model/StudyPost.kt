package com.study.platform.domain.post.model

import com.study.platform.domain.user.model.User
import com.study.platform.global.entity.BaseTimeEntity
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.model.TechStack
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "study_posts",
    indexes = [
        Index(name = "idx_study_post_status_deadline", columnList = "status, deadline"),
        Index(name = "idx_study_post_author_created", columnList = "author_id, created_at")
    ]
)
class StudyPost : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: User? = null

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "title", length = 100, nullable = false))
    private var titleVo: PostTitle? = null

    @Column(columnDefinition = "TEXT", nullable = false)
    var description: String = ""

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "tech_stack", length = 255))
    private var techStackVo: TechStack? = null

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "max_members", nullable = false))
    private var maxMembersVo: MaxMembers? = null

    var deadline: LocalDateTime? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StudyPostStatus = StudyPostStatus.OPEN

    val title: String get() = titleVo!!.value
    val techStack: String? get() = techStackVo?.value
    val maxMembers: Int get() = maxMembersVo!!.value

    fun update(title: String, description: String, techStack: String?, maxMembers: Int, deadline: LocalDateTime?) {
        this.titleVo = PostTitle(title)
        this.description = description
        this.techStackVo = TechStack.of(techStack)
        this.maxMembersVo = MaxMembers(maxMembers)
        this.deadline = deadline
    }

    fun close() { status = StudyPostStatus.CLOSED }
    fun markFull() { status = StudyPostStatus.FULL }

    fun markFullIfNeeded(approvedCount: Long) {
        if (approvedCount >= maxMembersVo!!.value) status = StudyPostStatus.FULL
    }

    fun isAuthor(userId: UUID): Boolean = author?.id == userId
    fun isOpen(): Boolean = status == StudyPostStatus.OPEN
    fun isFull(approvedCount: Long): Boolean = approvedCount >= maxMembersVo!!.value

    fun validateOpen() {
        if (!isOpen()) throw CustomException(ErrorCode.POST_CLOSED)
    }

    fun validateAuthor(userId: UUID) {
        if (!isAuthor(userId)) throw CustomException(ErrorCode.FORBIDDEN)
    }

    fun validateNotAuthor(userId: UUID) {
        if (isAuthor(userId)) throw CustomException(ErrorCode.CANNOT_APPLY_OWN_POST)
    }

    fun validateNotDuplicateApply(alreadyApplied: Boolean) {
        if (alreadyApplied) throw CustomException(ErrorCode.ALREADY_APPLIED)
    }

    companion object {
        fun create(author: User, title: String, description: String, techStack: String?, maxMembers: Int, deadline: LocalDateTime?): StudyPost =
            StudyPost().also {
                it.author = author
                it.titleVo = PostTitle(title)
                it.description = description
                it.techStackVo = TechStack.of(techStack)
                it.maxMembersVo = MaxMembers(maxMembers)
                it.deadline = deadline
            }
    }
}
