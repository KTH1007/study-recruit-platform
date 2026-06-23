package com.study.platform.domain.team.model

import com.study.platform.global.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "team_schedules")
class TeamSchedule : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    var team: StudyTeam? = null

    @Column(length = 100, nullable = false)
    var title: String = ""

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Column(nullable = false)
    var scheduledAt: LocalDateTime? = null

    fun update(title: String, description: String?, scheduledAt: LocalDateTime) {
        this.title = title
        this.description = description
        this.scheduledAt = scheduledAt
    }

    companion object {
        fun create(team: StudyTeam, title: String, description: String?, scheduledAt: LocalDateTime): TeamSchedule =
            TeamSchedule().also {
                it.team = team
                it.title = title
                it.description = description
                it.scheduledAt = scheduledAt
            }
    }
}
