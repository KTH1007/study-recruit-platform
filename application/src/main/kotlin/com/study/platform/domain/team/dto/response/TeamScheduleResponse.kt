package com.study.platform.domain.team.dto.response

import com.study.platform.domain.team.model.TeamSchedule
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

@Schema(description = "팀 일정 응답")
data class TeamScheduleResponse(

    @field:Schema(description = "일정 ID")
    val id: UUID,

    @field:Schema(description = "팀 ID")
    val teamId: UUID,

    @field:Schema(description = "일정 제목")
    val title: String,

    @field:Schema(description = "일정 내용")
    val description: String?,

    @field:Schema(description = "일정 날짜")
    val scheduledAt: LocalDateTime,

    @field:Schema(description = "생성일")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(schedule: TeamSchedule): TeamScheduleResponse = TeamScheduleResponse(
            id = schedule.id!!,
            teamId = schedule.team!!.id!!,
            title = schedule.title,
            description = schedule.description,
            scheduledAt = schedule.scheduledAt!!,
            createdAt = schedule.createdAt!!
        )
    }
}
