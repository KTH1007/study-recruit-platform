package com.study.platform.domain.notification.api;

import com.study.platform.domain.notification.application.NotificationService;
import com.study.platform.domain.notification.dto.response.NotificationResponse;
import com.study.platform.domain.notification.model.NotificationType;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private UUID userId;
    private UUID notificationId;
    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
        notificationResponse = new NotificationResponse(
                notificationId, userId, NotificationType.APPLY_RECEIVED,
                "새 지원이 있습니다", UUID.randomUUID(), false, LocalDateTime.now()
        );

        given(jwtProvider.getUserIdFromToken(anyString())).willReturn(userId);
    }

    @Test
    void findNotifications_성공() throws Exception {
        // given
        given(notificationService.findNotifications(any(), any()))
                .willReturn(new PageImpl<>(List.of(notificationResponse), PageRequest.of(0, 20), 1));

        // when & then
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].type").value("APPLY_RECEIVED"));
    }

    @Test
    void countUnread_성공() throws Exception {
        // given
        given(notificationService.countUnread(any())).willReturn(5L);

        // when & then
        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    void markAsRead_성공() throws Exception {
        // given
        willDoNothing().given(notificationService).markAsRead(any(), any());

        // when & then
        mockMvc.perform(patch("/api/notifications/{notificationId}/read", notificationId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void markAsRead_알림없음_404() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND))
                .given(notificationService).markAsRead(any(), any());

        // when & then
        mockMvc.perform(patch("/api/notifications/{notificationId}/read", notificationId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void markAsRead_본인알림아님_403() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.FORBIDDEN))
                .given(notificationService).markAsRead(any(), any());

        // when & then
        mockMvc.perform(patch("/api/notifications/{notificationId}/read", notificationId)
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void markAllAsRead_성공() throws Exception {
        // given
        willDoNothing().given(notificationService).markAllAsRead(any());

        // when & then
        mockMvc.perform(patch("/api/notifications/read-all")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
