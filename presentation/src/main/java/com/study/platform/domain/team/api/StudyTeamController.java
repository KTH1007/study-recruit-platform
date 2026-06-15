package com.study.platform.domain.team.api;

import com.study.platform.domain.team.api.doc.StudyTeamControllerDoc;
import com.study.platform.domain.team.dto.response.StudyTeamResponse;
import com.study.platform.domain.team.dto.response.TeamMemberResponse;
import com.study.platform.domain.team.usecase.*;
import com.study.platform.global.response.ApiResponse;
import com.study.platform.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class StudyTeamController implements StudyTeamControllerDoc {

    private final FindStudyTeamUseCase findStudyTeamUseCase;
    private final FindTeamMembersUseCase findTeamMembersUseCase;
    private final DelegateLeaderUseCase delegateLeaderUseCase;
    private final RemoveTeamMemberUseCase removeTeamMemberUseCase;
    private final LeaveTeamUseCase leaveTeamUseCase;

    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<StudyTeamResponse>> findTeam(@PathVariable UUID teamId) {
        return ApiResponse.success(SuccessCode.TEAM_FOUND, findStudyTeamUseCase.execute(teamId));
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> findMembers(@PathVariable UUID teamId) {
        return ApiResponse.success(SuccessCode.TEAM_MEMBER_LIST, findTeamMembersUseCase.execute(teamId));
    }

    @PatchMapping("/{teamId}/members/{targetUserId}/delegate")
    public ResponseEntity<ApiResponse<Void>> delegateLeader(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID targetUserId) {
        delegateLeaderUseCase.execute(userId, teamId, targetUserId);
        return ApiResponse.success(SuccessCode.LEADER_DELEGATED);
    }

    @DeleteMapping("/{teamId}/members/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID teamId,
            @PathVariable UUID targetUserId) {
        removeTeamMemberUseCase.execute(userId, teamId, targetUserId);
        return ApiResponse.success(SuccessCode.TEAM_MEMBER_REMOVED);
    }

    @DeleteMapping("/{teamId}/members/me")
    public ResponseEntity<ApiResponse<Void>> leaveTeam(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID teamId) {
        leaveTeamUseCase.execute(userId, teamId);
        return ApiResponse.success(SuccessCode.TEAM_LEFT);
    }
}
