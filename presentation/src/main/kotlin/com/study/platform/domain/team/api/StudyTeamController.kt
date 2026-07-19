package com.study.platform.domain.team.api

import com.study.platform.domain.team.api.doc.StudyTeamControllerDoc
import com.study.platform.domain.team.dto.response.StudyTeamResponse
import com.study.platform.domain.team.dto.response.TeamMemberResponse
import com.study.platform.domain.team.usecase.DelegateLeaderUseCase
import com.study.platform.domain.team.usecase.FindStudyTeamUseCase
import com.study.platform.domain.team.usecase.FindTeamMembersUseCase
import com.study.platform.domain.team.usecase.LeaveTeamUseCase
import com.study.platform.domain.team.usecase.RemoveTeamMemberUseCase
import com.study.platform.global.ratelimit.RateLimit
import com.study.platform.global.response.ApiResponse
import com.study.platform.global.response.SuccessCode
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/teams")
class StudyTeamController(
    private val findStudyTeamUseCase: FindStudyTeamUseCase,
    private val findTeamMembersUseCase: FindTeamMembersUseCase,
    private val delegateLeaderUseCase: DelegateLeaderUseCase,
    private val removeTeamMemberUseCase: RemoveTeamMemberUseCase,
    private val leaveTeamUseCase: LeaveTeamUseCase
) : StudyTeamControllerDoc {

    @GetMapping("/{teamId}")
    override fun findTeam(@PathVariable teamId: UUID): ResponseEntity<ApiResponse<StudyTeamResponse>> {
        return ApiResponse.success(SuccessCode.TEAM_FOUND, findStudyTeamUseCase.execute(teamId))
    }

    @GetMapping("/{teamId}/members")
    override fun findMembers(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable teamId: UUID
    ): ResponseEntity<ApiResponse<List<TeamMemberResponse>>> {
        return ApiResponse.success(SuccessCode.TEAM_MEMBER_LIST, findTeamMembersUseCase.execute(userId, teamId))
    }

    @PatchMapping("/{teamId}/members/{targetUserId}/delegate")
    @RateLimit(limit = 5, windowSeconds = 60)
    override fun delegateLeader(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable teamId: UUID,
        @PathVariable targetUserId: UUID
    ): ResponseEntity<ApiResponse<Void>> {
        delegateLeaderUseCase.execute(userId, teamId, targetUserId)
        return ApiResponse.success(SuccessCode.LEADER_DELEGATED)
    }

    @DeleteMapping("/{teamId}/members/{targetUserId}")
    @RateLimit(limit = 5, windowSeconds = 60)
    override fun removeMember(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable teamId: UUID,
        @PathVariable targetUserId: UUID
    ): ResponseEntity<ApiResponse<Void>> {
        removeTeamMemberUseCase.execute(userId, teamId, targetUserId)
        return ApiResponse.success(SuccessCode.TEAM_MEMBER_REMOVED)
    }

    @DeleteMapping("/{teamId}/members/me")
    override fun leaveTeam(
        @AuthenticationPrincipal userId: UUID,
        @PathVariable teamId: UUID
    ): ResponseEntity<ApiResponse<Void>> {
        leaveTeamUseCase.execute(userId, teamId)
        return ApiResponse.success(SuccessCode.TEAM_LEFT)
    }
}
