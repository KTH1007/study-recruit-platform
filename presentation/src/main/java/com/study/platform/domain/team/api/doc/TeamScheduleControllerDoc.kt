package com.study.platform.domain.team.api.doc

import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest
import com.study.platform.domain.team.dto.request.TeamScheduleUpdateRequest
import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import com.study.platform.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import java.util.UUID

@Tag(name = "TeamSchedule", description = "팀 일정 API")
interface TeamScheduleControllerDoc {

    @Operation(summary = "팀 일정 등록", description = "팀 일정을 등록합니다. 팀원 전체에게 알림이 발송됩니다.")
    fun createSchedule(
        @Parameter(hidden = true) userId: UUID,
        @Parameter(description = "팀 ID") teamId: UUID,
        request: TeamScheduleCreateRequest
    ): ResponseEntity<ApiResponse<TeamScheduleResponse>>

    @Operation(summary = "팀 일정 목록 조회", description = "팀 일정 목록을 날짜 오름차순으로 조회합니다.")
    fun findSchedules(
        @Parameter(hidden = true) userId: UUID,
        @Parameter(description = "팀 ID") teamId: UUID
    ): ResponseEntity<ApiResponse<List<TeamScheduleResponse>>>

    @Operation(summary = "팀 일정 수정", description = "팀 일정을 수정합니다.")
    fun updateSchedule(
        @Parameter(hidden = true) userId: UUID,
        @Parameter(description = "팀 ID") teamId: UUID,
        @Parameter(description = "일정 ID") scheduleId: UUID,
        request: TeamScheduleUpdateRequest
    ): ResponseEntity<ApiResponse<TeamScheduleResponse>>

    @Operation(summary = "팀 일정 삭제", description = "팀 일정을 삭제합니다.")
    fun deleteSchedule(
        @Parameter(hidden = true) userId: UUID,
        @Parameter(description = "팀 ID") teamId: UUID,
        @Parameter(description = "일정 ID") scheduleId: UUID
    ): ResponseEntity<ApiResponse<Void>>
}
