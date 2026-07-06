package com.study.platform.domain.team.api

import com.study.platform.domain.team.dto.response.StudyTeamResponse
import com.study.platform.domain.team.dto.response.TeamMemberResponse
import com.study.platform.domain.team.model.TeamMemberRole
import com.study.platform.domain.team.usecase.DelegateLeaderUseCase
import com.study.platform.domain.team.usecase.FindStudyTeamUseCase
import com.study.platform.domain.team.usecase.FindTeamMembersUseCase
import com.study.platform.domain.team.usecase.LeaveTeamUseCase
import com.study.platform.domain.team.usecase.RemoveTeamMemberUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.idempotency.IdempotencyObjectStoragePort
import com.study.platform.global.idempotency.IdempotencyStoragePort
import com.study.platform.global.jwt.JwtProvider
import com.study.platform.global.ratelimit.RateLimitStoragePort
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any as anyNonNull
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.mockito.BDDMockito.willThrow
import com.study.platform.support.TestSecurityConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(StudyTeamController::class)
@Import(TestSecurityConfig::class)
class StudyTeamControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var findStudyTeamUseCase: FindStudyTeamUseCase

    @MockitoBean
    private lateinit var findTeamMembersUseCase: FindTeamMembersUseCase

    @MockitoBean
    private lateinit var delegateLeaderUseCase: DelegateLeaderUseCase

    @MockitoBean
    private lateinit var removeTeamMemberUseCase: RemoveTeamMemberUseCase

    @MockitoBean
    private lateinit var leaveTeamUseCase: LeaveTeamUseCase

    @MockitoBean
    private lateinit var jwtProvider: JwtProvider

    @MockitoBean
    private lateinit var jpaMetamodelMappingContext: JpaMetamodelMappingContext

    @MockitoBean
    private lateinit var idempotencyObjectStoragePort: IdempotencyObjectStoragePort

    @MockitoBean
    private lateinit var idempotencyStoragePort: IdempotencyStoragePort

    @MockitoBean
    private lateinit var rateLimitStoragePort: RateLimitStoragePort

    private lateinit var userId: UUID
    private lateinit var teamId: UUID
    private lateinit var targetUserId: UUID
    private lateinit var teamResponse: StudyTeamResponse
    private lateinit var auth: UsernamePasswordAuthenticationToken

    @BeforeEach
    fun setUp() {
        userId = UUID.randomUUID()
        teamId = UUID.randomUUID()
        targetUserId = UUID.randomUUID()
        teamResponse = StudyTeamResponse(teamId, UUID.randomUUID(), "스터디 모집")
        auth = UsernamePasswordAuthenticationToken(userId, null, listOf())

        given(jwtProvider.getUserIdFromToken(anyString())).willReturn(userId)
    }

    @Test
    fun `findTeam_성공`() {
        given(findStudyTeamUseCase.execute(anyNonNull())).willReturn(teamResponse)

        mockMvc.perform(get("/api/teams/{teamId}", teamId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("스터디 모집"))
    }

    @Test
    fun `findTeam_존재하지않음_404`() {
        given(findStudyTeamUseCase.execute(anyNonNull()))
            .willThrow(CustomException(ErrorCode.TEAM_NOT_FOUND))

        mockMvc.perform(get("/api/teams/{teamId}", teamId))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `findMembers_성공`() {
        val members = listOf(
            TeamMemberResponse(UUID.randomUUID(), userId, "팀장", TeamMemberRole.LEADER),
            TeamMemberResponse(UUID.randomUUID(), targetUserId, "팀원", TeamMemberRole.MEMBER)
        )
        given(findTeamMembersUseCase.execute(anyNonNull(), anyNonNull())).willReturn(members)

        mockMvc.perform(
            get("/api/teams/{teamId}/members", teamId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()").value(2))
    }

    @Test
    fun `findMembers_팀원아님_403`() {
        willThrow(CustomException(ErrorCode.NOT_TEAM_MEMBER))
            .given(findTeamMembersUseCase).execute(anyNonNull(), anyNonNull())

        mockMvc.perform(
            get("/api/teams/{teamId}/members", teamId)
                .with(authentication(auth))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `delegateLeader_성공`() {
        willDoNothing().given(delegateLeaderUseCase).execute(anyNonNull(), anyNonNull(), anyNonNull())

        mockMvc.perform(
            patch("/api/teams/{teamId}/members/{targetUserId}/delegate", teamId, targetUserId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `delegateLeader_권한없음_403`() {
        willThrow(CustomException(ErrorCode.FORBIDDEN))
            .given(delegateLeaderUseCase).execute(anyNonNull(), anyNonNull(), anyNonNull())

        mockMvc.perform(
            patch("/api/teams/{teamId}/members/{targetUserId}/delegate", teamId, targetUserId)
                .with(authentication(auth))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `removeMember_성공`() {
        willDoNothing().given(removeTeamMemberUseCase).execute(anyNonNull(), anyNonNull(), anyNonNull())

        mockMvc.perform(
            delete("/api/teams/{teamId}/members/{targetUserId}", teamId, targetUserId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `leaveTeam_성공`() {
        willDoNothing().given(leaveTeamUseCase).execute(anyNonNull(), anyNonNull())

        mockMvc.perform(
            delete("/api/teams/{teamId}/members/me", teamId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `leaveTeam_리더위임필요_400`() {
        willThrow(CustomException(ErrorCode.LEADER_MUST_DELEGATE))
            .given(leaveTeamUseCase).execute(anyNonNull(), anyNonNull())

        mockMvc.perform(
            delete("/api/teams/{teamId}/members/me", teamId)
                .with(authentication(auth))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }
}
