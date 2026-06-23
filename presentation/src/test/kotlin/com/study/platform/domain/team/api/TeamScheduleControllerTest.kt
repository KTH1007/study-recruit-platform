package com.study.platform.domain.team.api

import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest
import com.study.platform.domain.team.dto.request.TeamScheduleUpdateRequest
import com.study.platform.domain.team.dto.response.TeamScheduleResponse
import com.study.platform.domain.team.usecase.CreateTeamScheduleUseCase
import com.study.platform.domain.team.usecase.DeleteTeamScheduleUseCase
import com.study.platform.domain.team.usecase.FindTeamSchedulesUseCase
import com.study.platform.domain.team.usecase.UpdateTeamScheduleUseCase
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.idempotency.IdempotencyObjectStoragePort
import com.study.platform.global.idempotency.IdempotencyStoragePort
import com.study.platform.global.jwt.JwtProvider
import com.study.platform.global.ratelimit.RateLimitStoragePort
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any as anyNonNull
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.mockito.BDDMockito.willThrow
import com.study.platform.support.TestSecurityConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.util.UUID

@WebMvcTest(TeamScheduleController::class)
@Import(TestSecurityConfig::class)
class TeamScheduleControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var createTeamScheduleUseCase: CreateTeamScheduleUseCase

    @MockitoBean
    private lateinit var findTeamSchedulesUseCase: FindTeamSchedulesUseCase

    @MockitoBean
    private lateinit var updateTeamScheduleUseCase: UpdateTeamScheduleUseCase

    @MockitoBean
    private lateinit var deleteTeamScheduleUseCase: DeleteTeamScheduleUseCase

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

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var userId: UUID
    private lateinit var teamId: UUID
    private lateinit var scheduleId: UUID
    private lateinit var scheduleResponse: TeamScheduleResponse
    private lateinit var auth: UsernamePasswordAuthenticationToken

    @BeforeEach
    fun setUp() {
        userId = UUID.randomUUID()
        teamId = UUID.randomUUID()
        scheduleId = UUID.randomUUID()
        scheduleResponse = TeamScheduleResponse(
            scheduleId, teamId, "1회차 미팅", "미팅 내용",
            LocalDateTime.now().plusDays(3), LocalDateTime.now()
        )
        auth = UsernamePasswordAuthenticationToken(userId, null, listOf())

        given(jwtProvider.getUserIdFromToken(anyString())).willReturn(userId)
        given(rateLimitStoragePort.isAllowed(anyString(), anyLong(), anyLong())).willReturn(true)
    }

    @Test
    fun `createSchedule_성공`() {
        val request = TeamScheduleCreateRequest("1회차 미팅", "미팅 내용", LocalDateTime.now().plusDays(3))
        given(createTeamScheduleUseCase.execute(anyNonNull(), anyNonNull(), anyNonNull())).willReturn(scheduleResponse)

        mockMvc.perform(
            post("/api/teams/{teamId}/schedules", teamId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.title").value("1회차 미팅"))
    }

    @Test
    fun `createSchedule_제목빈값_400`() {
        val request = TeamScheduleCreateRequest("", "미팅 내용", LocalDateTime.now().plusDays(3))

        mockMvc.perform(
            post("/api/teams/{teamId}/schedules", teamId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `createSchedule_날짜없음_400`() {
        val body = """{"title":"1회차 미팅","description":"내용","scheduledAt":null}"""

        mockMvc.perform(
            post("/api/teams/{teamId}/schedules", teamId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `createSchedule_팀멤버아님_403`() {
        val request = TeamScheduleCreateRequest("1회차 미팅", "미팅 내용", LocalDateTime.now().plusDays(3))
        given(createTeamScheduleUseCase.execute(anyNonNull(), anyNonNull(), anyNonNull()))
            .willThrow(CustomException(ErrorCode.NOT_TEAM_MEMBER))

        mockMvc.perform(
            post("/api/teams/{teamId}/schedules", teamId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `findSchedules_성공`() {
        given(findTeamSchedulesUseCase.execute(anyNonNull(), anyNonNull())).willReturn(listOf(scheduleResponse))

        mockMvc.perform(
            get("/api/teams/{teamId}/schedules", teamId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].title").value("1회차 미팅"))
    }

    @Test
    fun `updateSchedule_성공`() {
        val request = TeamScheduleUpdateRequest("2회차 미팅", "수정된 내용", LocalDateTime.now().plusDays(7))
        val updated = TeamScheduleResponse(
            scheduleId, teamId, "2회차 미팅", "수정된 내용",
            LocalDateTime.now().plusDays(7), LocalDateTime.now()
        )
        given(updateTeamScheduleUseCase.execute(anyNonNull(), anyNonNull(), anyNonNull(), anyNonNull())).willReturn(updated)

        mockMvc.perform(
            patch("/api/teams/{teamId}/schedules/{scheduleId}", teamId, scheduleId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.title").value("2회차 미팅"))
    }

    @Test
    fun `deleteSchedule_성공`() {
        willDoNothing().given(deleteTeamScheduleUseCase).execute(anyNonNull(), anyNonNull(), anyNonNull())

        mockMvc.perform(
            delete("/api/teams/{teamId}/schedules/{scheduleId}", teamId, scheduleId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `deleteSchedule_일정없음_404`() {
        willThrow(CustomException(ErrorCode.TEAM_SCHEDULE_NOT_FOUND))
            .given(deleteTeamScheduleUseCase).execute(anyNonNull(), anyNonNull(), anyNonNull())

        mockMvc.perform(
            delete("/api/teams/{teamId}/schedules/{scheduleId}", teamId, scheduleId)
                .with(authentication(auth))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }
}
