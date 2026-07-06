package com.study.platform.domain.apply.api

import com.study.platform.domain.apply.dto.request.ApplyCreateRequest
import com.study.platform.domain.apply.dto.response.ApplyResponse
import com.study.platform.domain.apply.model.ApplyStatus
import com.study.platform.domain.apply.usecase.ApplyStudyPostUseCase
import com.study.platform.domain.apply.usecase.ApproveApplyUseCase
import com.study.platform.domain.apply.usecase.CancelApplyUseCase
import com.study.platform.domain.apply.usecase.FindAppliesUseCase
import com.study.platform.domain.apply.usecase.RejectApplyUseCase
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
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.data.domain.PageImpl
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

@WebMvcTest(ApplyController::class)
@Import(TestSecurityConfig::class)
class ApplyControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var findAppliesUseCase: FindAppliesUseCase

    @MockitoBean
    private lateinit var applyStudyPostUseCase: ApplyStudyPostUseCase

    @MockitoBean
    private lateinit var cancelApplyUseCase: CancelApplyUseCase

    @MockitoBean
    private lateinit var approveApplyUseCase: ApproveApplyUseCase

    @MockitoBean
    private lateinit var rejectApplyUseCase: RejectApplyUseCase

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
    private lateinit var postId: UUID
    private lateinit var applyId: UUID
    private lateinit var applyResponse: ApplyResponse
    private lateinit var auth: UsernamePasswordAuthenticationToken

    @BeforeEach
    fun setUp() {
        userId = UUID.randomUUID()
        postId = UUID.randomUUID()
        applyId = UUID.randomUUID()
        applyResponse = ApplyResponse(UUID.randomUUID(), "지원자", "Java", "지원합니다", ApplyStatus.PENDING, LocalDateTime.now())
        auth = UsernamePasswordAuthenticationToken(userId, null, listOf())

        given(jwtProvider.getUserIdFromToken(anyString())).willReturn(userId)
        given(rateLimitStoragePort.isAllowed(anyString(), anyLong(), anyLong())).willReturn(true)
    }

    @Test
    fun `findApplies_성공`() {
        given(findAppliesUseCase.execute(anyNonNull(), anyNonNull(), anyNonNull())).willReturn(
            PageImpl(listOf(applyResponse))
        )

        mockMvc.perform(
            get("/api/posts/{postId}/applies", postId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content[0].applicantNickname").value("지원자"))
    }

    @Test
    fun `apply_성공`() {
        val request = ApplyCreateRequest("지원합니다")
        given(applyStudyPostUseCase.execute(anyNonNull(), anyNonNull(), anyNonNull())).willReturn(applyResponse)

        mockMvc.perform(
            post("/api/posts/{postId}/applies", postId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `apply_메시지500자초과_400`() {
        val request = ApplyCreateRequest("a".repeat(501))

        mockMvc.perform(
            post("/api/posts/{postId}/applies", postId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `cancel_성공`() {
        willDoNothing().given(cancelApplyUseCase).execute(anyNonNull(), anyNonNull())

        mockMvc.perform(
            delete("/api/posts/{postId}/applies", postId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `approve_성공`() {
        val approvedResponse = ApplyResponse(applyId, "지원자", "Java", "지원합니다", ApplyStatus.APPROVED, LocalDateTime.now())
        given(approveApplyUseCase.execute(anyNonNull(), anyNonNull())).willReturn(approvedResponse)

        mockMvc.perform(
            patch("/api/applies/{applyId}/approve", applyId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.status").value("APPROVED"))
    }

    @Test
    fun `approve_권한없음_403`() {
        willThrow(CustomException(ErrorCode.FORBIDDEN)).given(approveApplyUseCase).execute(anyNonNull(), anyNonNull())

        mockMvc.perform(
            patch("/api/applies/{applyId}/approve", applyId)
                .with(authentication(auth))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `reject_성공`() {
        val rejectedResponse = ApplyResponse(applyId, "지원자", "Java", "지원합니다", ApplyStatus.REJECTED, LocalDateTime.now())
        given(rejectApplyUseCase.execute(anyNonNull(), anyNonNull())).willReturn(rejectedResponse)

        mockMvc.perform(
            patch("/api/applies/{applyId}/reject", applyId)
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.status").value("REJECTED"))
    }

    @Test
    fun `approve_락충돌_409`() {
        willThrow(PessimisticLockingFailureException("lock timeout"))
            .given(approveApplyUseCase).execute(anyNonNull(), anyNonNull())

        mockMvc.perform(
            patch("/api/applies/{applyId}/approve", applyId)
                .with(authentication(auth))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.success").value(false))
    }
}
