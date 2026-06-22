package com.study.platform.domain.team.api

import com.study.platform.domain.team.api.doc.TeamScheduleControllerDoc
import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest
import com.study.platform.domain.team.dto.request.TeamScheduleUpdateRequest
import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import com.study.platform.domain.team.usecase.CreateTeamScheduleUseCase
import com.study.platform.domain.team.usecase.DeleteTeamScheduleUseCase
import com.study.platform.domain.team.usecase.FindTeamSchedulesUseCase
import com.study.platform.domain.team.usecase.UpdateTeamScheduleUseCase
import com.study.platform.global.idempotency.Idempotent
import com.study.platform.global.ratelimit.RateLimit
import com.study.platform.global.response.ApiResponse
import com.study.platform.global.response.SuccessCode
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/teams/{teamId}/schedules")
class TeamScheduleController(
    private val createTeamScheduleUseCase: CreateTeamScheduleUseCase,
    private val findTeamSchedulesUseCase: FindTeamSchedulesUseCase,
    private val updateTeamScheduleUseCase: UpdateTeamScheduleUseCase,
    private val deleteTeamScheduleUseCase: DeleteTeamScheduleUseCase
) : TeamScheduleControllerDoc {

    @Idempotent
    @RateLimit(limit = 5, windowSeconds = 60)
    @PostMapping
    override fun createSchedule(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable teamId: UUID,
        @Valid @RequestBody request: TeamScheduleCreateRequest
    ): ResponseEntity<ApiResponse<TeamScheduleResponse>> {
        return ApiResponse.success(SuccessCode.TEAM_SCHEDULE_CREATED, createTeamScheduleUseCase.execute(userId, teamId, request))
    }

    @GetMapping
    override fun findSchedules(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable teamId: UUID
    ): ResponseEntity<ApiResponse<List<TeamScheduleResponse>>> {
        return ApiResponse.success(SuccessCode.TEAM_SCHEDULE_LIST, findTeamSchedulesUseCase.execute(userId, teamId))
    }

    @PatchMapping("/{scheduleId}")
    override fun updateSchedule(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable teamId: UUID,
        @PathVariable scheduleId: UUID,
        @Valid @RequestBody request: TeamScheduleUpdateRequest
    ): ResponseEntity<ApiResponse<TeamScheduleResponse>> {
        return ApiResponse.success(SuccessCode.TEAM_SCHEDULE_UPDATED, updateTeamScheduleUseCase.execute(userId, teamId, scheduleId, request))
    }

    @DeleteMapping("/{scheduleId}")
    override fun deleteSchedule(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable teamId: UUID,
        @PathVariable scheduleId: UUID
    ): ResponseEntity<ApiResponse<Void>> {
        deleteTeamScheduleUseCase.execute(userId, teamId, scheduleId)
        return ApiResponse.success(SuccessCode.TEAM_SCHEDULE_DELETED)
    }
}
