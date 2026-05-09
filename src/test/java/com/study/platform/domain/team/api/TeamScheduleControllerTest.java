package com.study.platform.domain.team.api;

import com.study.platform.domain.team.application.TeamScheduleService;
import com.study.platform.domain.team.dto.request.TeamScheduleCreateRequest;
import com.study.platform.domain.team.dto.request.TeamScheduleUpdateRequest;
import com.study.platform.domain.team.dto.response.TeamScheduleResponse;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.redis.core.RedisTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamScheduleController.class)
class TeamScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamScheduleService teamScheduleService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID teamId;
    private UUID scheduleId;
    private TeamScheduleResponse scheduleResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
        scheduleResponse = new TeamScheduleResponse(
                scheduleId, teamId, "1회차 미팅", "미팅 내용",
                LocalDateTime.now().plusDays(3), LocalDateTime.now()
        );

        given(jwtProvider.getUserIdFromToken(anyString())).willReturn(userId);
    }

    @Test
    void createSchedule_성공() throws Exception {
        // given
        TeamScheduleCreateRequest request = new TeamScheduleCreateRequest(
                "1회차 미팅", "미팅 내용", LocalDateTime.now().plusDays(3));
        given(teamScheduleService.createSchedule(any(), any(), any())).willReturn(scheduleResponse);

        // when & then
        mockMvc.perform(post("/api/teams/{teamId}/schedules", teamId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("1회차 미팅"));
    }

    @Test
    void createSchedule_제목빈값_400() throws Exception {
        // given
        TeamScheduleCreateRequest request = new TeamScheduleCreateRequest(
                "", "미팅 내용", LocalDateTime.now().plusDays(3));

        // when & then
        mockMvc.perform(post("/api/teams/{teamId}/schedules", teamId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSchedule_날짜없음_400() throws Exception {
        // given
        TeamScheduleCreateRequest request = new TeamScheduleCreateRequest("1회차 미팅", "내용", null);

        // when & then
        mockMvc.perform(post("/api/teams/{teamId}/schedules", teamId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSchedule_팀멤버아님_403() throws Exception {
        // given
        TeamScheduleCreateRequest request = new TeamScheduleCreateRequest(
                "1회차 미팅", "미팅 내용", LocalDateTime.now().plusDays(3));
        given(teamScheduleService.createSchedule(any(), any(), any()))
                .willThrow(new CustomException(ErrorCode.NOT_TEAM_MEMBER));

        // when & then
        mockMvc.perform(post("/api/teams/{teamId}/schedules", teamId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void findSchedules_성공() throws Exception {
        // given
        given(teamScheduleService.findSchedules(any(), any())).willReturn(List.of(scheduleResponse));

        // when & then
        mockMvc.perform(get("/api/teams/{teamId}/schedules", teamId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("1회차 미팅"));
    }

    @Test
    void updateSchedule_성공() throws Exception {
        // given
        TeamScheduleUpdateRequest request = new TeamScheduleUpdateRequest(
                "2회차 미팅", "수정된 내용", LocalDateTime.now().plusDays(7));
        TeamScheduleResponse updated = new TeamScheduleResponse(
                scheduleId, teamId, "2회차 미팅", "수정된 내용",
                LocalDateTime.now().plusDays(7), LocalDateTime.now()
        );
        given(teamScheduleService.updateSchedule(any(), any(), any(), any())).willReturn(updated);

        // when & then
        mockMvc.perform(patch("/api/teams/{teamId}/schedules/{scheduleId}", teamId, scheduleId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("2회차 미팅"));
    }

    @Test
    void deleteSchedule_성공() throws Exception {
        // given
        willDoNothing().given(teamScheduleService).deleteSchedule(any(), any(), any());

        // when & then
        mockMvc.perform(delete("/api/teams/{teamId}/schedules/{scheduleId}", teamId, scheduleId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteSchedule_일정없음_404() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.TEAM_SCHEDULE_NOT_FOUND))
                .given(teamScheduleService).deleteSchedule(any(), any(), any());

        // when & then
        mockMvc.perform(delete("/api/teams/{teamId}/schedules/{scheduleId}", teamId, scheduleId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
