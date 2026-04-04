package com.study.platform.domain.team.api.doc;

import com.study.platform.domain.team.dto.response.StudyTeamResponse;
import com.study.platform.domain.team.dto.response.TeamMemberResponse;
import com.study.platform.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "StudyTeam", description = "스터디팀 API")
public interface StudyTeamControllerDoc {

    @Operation(summary = "팀 조회", description = "팀 정보를 조회합니다.")
    ResponseEntity<ApiResponse<StudyTeamResponse>> findTeam(UUID teamId);

    @Operation(summary = "팀원 목록 조회", description = "팀원 목록을 조회합니다.")
    ResponseEntity<ApiResponse<List<TeamMemberResponse>>> findMembers(UUID teamId);

    @Operation(summary = "리더 위임", description = "팀장 권한을 다른 팀원에게 위임합니다.")
    ResponseEntity<ApiResponse<Void>> delegateLeader(@Parameter(hidden = true) UUID userId, UUID teamId, UUID targetUserId);

    @Operation(summary = "팀원 추방", description = "팀장이 특정 팀원을 추방합니다.")
    ResponseEntity<ApiResponse<Void>> removeMember(@Parameter(hidden = true) UUID userId, UUID teamId, UUID targerUserId);

    @Operation(summary = "팀 탈퇴", description = "팀을 탈퇴합니다. 마지막 팀원이면 팀이 삭제됩니다.")
    ResponseEntity<ApiResponse<Void>> leaveTeam(@Parameter(hidden = true) UUID userId, UUID teamId);
}
