package com.study.platform.domain.team.api;

import com.study.platform.domain.team.api.doc.TeamScheduleControllerDoc;
import com.study.platform.domain.team.application.TeamScheduleService;
import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest;
import com.study.platform.domain.team.dto.request.TeamScheduleUpdateRequest;
import com.study.platform.domain.team.dto.response.TeamScheduleResponse;
import com.study.platform.global.idempotency.Idempotent;
import com.study.platform.global.ratelimit.RateLimit;
import com.study.platform.global.response.ApiResponse;
import com.study.platform.global.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams/{teamId}/schedules")
public class TeamScheduleController implements TeamScheduleControllerDoc {

    private final TeamScheduleService teamScheduleService;

    @Idempotent
    @RateLimit(limit = 5, windowSeconds = 60)
    @PostMapping
    public ResponseEntity<ApiResponse<TeamScheduleResponse>> createSchedule(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID teamId,
            @Valid @RequestBody TeamScheduleCreateRequest request) {
        return ApiResponse.success(SuccessCode.TEAM_SCHEDULE_CREATED, teamScheduleService.createSchedule(userId, teamId, request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamScheduleResponse>>> findSchedules(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID teamId) {
        return ApiResponse.success(SuccessCode.TEAM_SCHEDULE_LIST, teamScheduleService.findSchedules(userId, teamId));
    }

    @PatchMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<TeamScheduleResponse>> updateSchedule(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID scheduleId,
            @Valid @RequestBody TeamScheduleUpdateRequest request) {
        return ApiResponse.success(SuccessCode.TEAM_SCHEDULE_UPDATED, teamScheduleService.updateSchedule(userId, teamId, scheduleId, request));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID scheduleId) {
        teamScheduleService.deleteSchedule(userId, teamId, scheduleId);
        return ApiResponse.success(SuccessCode.TEAM_SCHEDULE_DELETED);
    }
}
