package com.study.platform.domain.team.api;

import com.study.platform.domain.team.application.StudyTeamService;
import com.study.platform.domain.team.dto.response.StudyTeamResponse;
import com.study.platform.domain.team.dto.response.TeamMemberResponse;
import com.study.platform.domain.team.model.TeamMemberRole;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudyTeamController.class)
class StudyTeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudyTeamService studyTeamService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private UUID userId;
    private UUID teamId;
    private UUID targetUserId;
    private StudyTeamResponse teamResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
        teamResponse = new StudyTeamResponse(teamId, UUID.randomUUID(), "스터디 모집");

        given(jwtProvider.getUserIdFromToken(anyString())).willReturn(userId);
    }

    @Test
    void findTeam_성공() throws Exception {
        // given
        given(studyTeamService.findTeam(any())).willReturn(teamResponse);

        // when & then
        mockMvc.perform(get("/api/teams/{teamId}", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("스터디 모집"));
    }

    @Test
    void findTeam_존재하지않음_404() throws Exception {
        // given
        given(studyTeamService.findTeam(any()))
                .willThrow(new CustomException(ErrorCode.TEAM_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/teams/{teamId}", teamId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void findMembers_성공() throws Exception {
        // given
        List<TeamMemberResponse> members = List.of(
                new TeamMemberResponse(UUID.randomUUID(), userId, "팀장", TeamMemberRole.LEADER),
                new TeamMemberResponse(UUID.randomUUID(), targetUserId, "팀원", TeamMemberRole.MEMBER)
        );
        given(studyTeamService.findMembers(any())).willReturn(members);

        // when & then
        mockMvc.perform(get("/api/teams/{teamId}/members", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void delegateLeader_성공() throws Exception {
        // given
        willDoNothing().given(studyTeamService).delegateLeader(any(), any(), any());

        // when & then
        mockMvc.perform(patch("/api/teams/{teamId}/members/{targetUserId}/delegate", teamId, targetUserId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void delegateLeader_권한없음_403() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.FORBIDDEN))
                .given(studyTeamService).delegateLeader(any(), any(), any());

        // when & then
        mockMvc.perform(patch("/api/teams/{teamId}/members/{targetUserId}/delegate", teamId, targetUserId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void removeMember_성공() throws Exception {
        // given
        willDoNothing().given(studyTeamService).removeMember(any(), any(), any());

        // when & then
        mockMvc.perform(delete("/api/teams/{teamId}/members/{targetUserId}", teamId, targetUserId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void leaveTeam_성공() throws Exception {
        // given
        willDoNothing().given(studyTeamService).leaveTeam(any(), any());

        // when & then
        mockMvc.perform(delete("/api/teams/{teamId}/members/me", teamId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void leaveTeam_리더위임필요_400() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.LEADER_MUST_DELEGATE))
                .given(studyTeamService).leaveTeam(any(), any());

        // when & then
        mockMvc.perform(delete("/api/teams/{teamId}/members/me", teamId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
