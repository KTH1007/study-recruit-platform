package com.study.platform.domain.team.model

import com.study.platform.domain.post.model.StudyPost
import com.study.platform.global.entity.BaseTimeEntity
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "study_teams")
class StudyTeam : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    var id: UUID? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    var post: StudyPost? = null

    @Column(length = 100, nullable = false)
    var name: String = ""

    companion object {
        fun create(post: StudyPost): StudyTeam = StudyTeam().also {
            it.post = post
            it.name = post.title
        }
    }
}
