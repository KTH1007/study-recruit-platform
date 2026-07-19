package com.study.platform.domain.team.model

import com.study.platform.global.entity.BaseTimeEntity
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
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
        validateTitle(title)
        validateDescription(description)
        this.title = title
        this.description = description
        this.scheduledAt = scheduledAt
    }

    fun validateBelongsToTeam(teamId: UUID) {
        if (team?.id != teamId) throw CustomException(ErrorCode.TEAM_SCHEDULE_NOT_FOUND)
    }

    companion object {
        private const val MAX_TITLE_LENGTH = 100
        private const val MAX_DESCRIPTION_LENGTH = 1000

        fun create(team: StudyTeam, title: String, description: String?, scheduledAt: LocalDateTime): TeamSchedule {
            validateTitle(title)
            validateDescription(description)
            return TeamSchedule().also {
                it.team = team
                it.title = title
                it.description = description
                it.scheduledAt = scheduledAt
            }
        }

        private fun validateTitle(title: String) {
            if (title.isBlank()) throw CustomException(ErrorCode.INVALID_INPUT)
            if (title.length > MAX_TITLE_LENGTH) throw CustomException(ErrorCode.INVALID_INPUT)
        }

        private fun validateDescription(description: String?) {
            if (description != null && description.length > MAX_DESCRIPTION_LENGTH) throw CustomException(ErrorCode.INVALID_INPUT)
        }
    }
}
