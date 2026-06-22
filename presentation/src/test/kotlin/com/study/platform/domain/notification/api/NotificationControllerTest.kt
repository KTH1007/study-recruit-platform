package com.study.platform.domain.notification.api

import com.study.platform.domain.notification.dto.response.NotificationResponse
import com.study.platform.domain.notification.model.NotificationType
import com.study.platform.domain.notification.usecase.CountUnreadNotificationsUseCase
import com.study.platform.domain.notification.usecase.FindNotificationsUseCase
import com.study.platform.domain.notification.usecase.MarkAllNotificationsAsReadUseCase
import com.study.platform.domain.notification.usecase.MarkNotificationAsReadUseCase
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.util.UUID

@WebMvcTest(NotificationController::class)
@Import(TestSecurityConfig::class)
class NotificationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var findNotificationsUseCase: FindNotificationsUseCase

    @MockitoBean
    private lateinit var countUnreadNotificationsUseCase: CountUnreadNotificationsUseCase

    @MockitoBean
    private lateinit var markNotificationAsReadUseCase: MarkNotificationAsReadUseCase

    @MockitoBean
    private lateinit var markAllNotificationsAsReadUseCase: MarkAllNotificationsAsReadUseCase

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
    private lateinit var notificationId: UUID
    private lateinit var notificationResponse: NotificationResponse
    private lateinit var auth: UsernamePasswordAuthenticationToken

    @BeforeEach
    fun setUp() {
        userId = UUID.randomUUID()
        notificationId = UUID.randomUUID()
        notificationResponse = NotificationResponse(
            notificationId, userId, NotificationType.APPLY_RECEIVED,
            "새 지원이 있습니다", UUID.randomUUID(), false, LocalDateTime.now()
        )
        auth = UsernamePasswordAuthenticationToken(userId, null, listOf())

        given(jwtProvider.getUserIdFromToken(anyString())).willReturn(userId)
    }

    @Test
    fun `findNotifications_성공`() {
        given(findNotificationsUseCase.execute(anyNonNull(), anyNonNull()))
            .willReturn(PageImpl(listOf(notificationResponse), PageRequest.of(0, 20), 1))

        mockMvc.perform(
            get("/api/notifications")
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content[0].type").value("APPLY_RECEIVED"))
    }

    @Test
    fun `countUnread_성공`() {
        given(countUnreadNotificationsUseCase.execute(anyNonNull())).willReturn(5L)

        mockMvc.perform(
            get("/api/notifications/unread-count")
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value(5))
    }

    @Test
    fun `markAsRead_성공`() {
        willDoNothing().given(markNotificationAsReadUseCase).execute(anyNonNull(), anyNonNull())

        mockMvc.perform(
            patch("/api/notifications/{notificationId}/read", notificationId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `markAsRead_알림없음_404`() {
        willThrow(CustomException(ErrorCode.NOTIFICATION_NOT_FOUND))
            .given(markNotificationAsReadUseCase).execute(anyNonNull(), anyNonNull())

        mockMvc.perform(
            patch("/api/notifications/{notificationId}/read", notificationId)
                .with(authentication(auth))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `markAsRead_본인알림아님_403`() {
        willThrow(CustomException(ErrorCode.FORBIDDEN))
            .given(markNotificationAsReadUseCase).execute(anyNonNull(), anyNonNull())

        mockMvc.perform(
            patch("/api/notifications/{notificationId}/read", notificationId)
                .with(authentication(auth))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `markAllAsRead_성공`() {
        willDoNothing().given(markAllNotificationsAsReadUseCase).execute(anyNonNull())

        mockMvc.perform(
            patch("/api/notifications/read-all")
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }
}
