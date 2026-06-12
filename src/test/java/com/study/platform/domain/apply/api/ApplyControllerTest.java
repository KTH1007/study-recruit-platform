package com.study.platform.domain.apply.api;
import com.study.platform.global.idempotency.IdempotencyObjectStoragePort;
import com.study.platform.global.idempotency.IdempotencyStoragePort;
import com.study.platform.global.ratelimit.RateLimitStoragePort;

import com.study.platform.domain.apply.application.ApplyService;
import com.study.platform.domain.apply.dto.request.ApplyCreateRequest;
import com.study.platform.domain.apply.dto.response.ApplyResponse;
import com.study.platform.domain.apply.model.ApplyStatus;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.PessimisticLockingFailureException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplyController.class)
class ApplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplyService applyService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private IdempotencyObjectStoragePort idempotencyObjectStoragePort;

    @MockitoBean
    private IdempotencyStoragePort idempotencyStoragePort;

    @MockitoBean
    private RateLimitStoragePort rateLimitStoragePort;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID postId;
    private UUID applyId;
    private ApplyResponse applyResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        applyId = UUID.randomUUID();
        applyResponse = new ApplyResponse(UUID.randomUUID(), "지원자", "Java", "지원합니다", ApplyStatus.PENDING, LocalDateTime.now());

        given(jwtProvider.getUserIdFromToken(anyString())).willReturn(userId);
    }

    @Test
    void findApplies_성공() throws Exception {
        // given
        given(applyService.findApplies(any(), any())).willReturn(List.of(applyResponse));

        // when & then
        mockMvc.perform(get("/api/posts/{postId}/applies", postId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].applicantNickname").value("지원자"));
    }

    @Test
    void apply_성공() throws Exception {
        // given
        ApplyCreateRequest request = new ApplyCreateRequest("지원합니다");
        given(applyService.apply(any(), any(), any())).willReturn(applyResponse);

        // when & then
        mockMvc.perform(post("/api/posts/{postId}/applies", postId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void apply_메시지500자초과_400() throws Exception {
        // given
        ApplyCreateRequest request = new ApplyCreateRequest("a".repeat(501));

        // when & then
        mockMvc.perform(post("/api/posts/{postId}/applies", postId)
                        .header("Authorization", "Bearer fake-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancel_성공() throws Exception {
        // given
        willDoNothing().given(applyService).cancel(any(), any());

        // when & then
        mockMvc.perform(delete("/api/posts/{postId}/applies", postId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void approve_성공() throws Exception {
        // given
        ApplyResponse approvedResponse = new ApplyResponse(applyId, "지원자", "Java", "지원합니다", ApplyStatus.APPROVED, LocalDateTime.now());
        given(applyService.approve(any(), any())).willReturn(approvedResponse);

        // when & then
        mockMvc.perform(patch("/api/applies/{applyId}/approve", applyId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void approve_권한없음_403() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.FORBIDDEN)).given(applyService).approve(any(), any());

        // when & then
        mockMvc.perform(patch("/api/applies/{applyId}/approve", applyId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void reject_성공() throws Exception {
        // given
        ApplyResponse rejectedResponse = new ApplyResponse(applyId, "지원자", "Java", "지원합니다", ApplyStatus.REJECTED, LocalDateTime.now());
        given(applyService.reject(any(), any())).willReturn(rejectedResponse);

        // when & then
        mockMvc.perform(patch("/api/applies/{applyId}/reject", applyId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    void approve_락충돌_409() throws Exception {
        // given
        willThrow(new PessimisticLockingFailureException("lock timeout"))
                .given(applyService).approve(any(), any());

        // when & then
        mockMvc.perform(patch("/api/applies/{applyId}/approve", applyId)
                .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }
}
