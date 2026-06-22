package com.study.platform.domain.user.api

import com.study.platform.domain.user.application.AuthService
import com.study.platform.domain.user.dto.request.LoginRequest
import com.study.platform.domain.user.dto.request.TokenReissueRequest
import com.study.platform.domain.user.dto.response.LoginResponse
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@WebMvcTest(AuthController::class)
@Import(TestSecurityConfig::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var authService: AuthService

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
    private lateinit var loginResponse: LoginResponse
    private lateinit var auth: UsernamePasswordAuthenticationToken

    @BeforeEach
    fun setUp() {
        userId = UUID.randomUUID()
        loginResponse = LoginResponse.of("access-token", "refresh-token")
        auth = UsernamePasswordAuthenticationToken(userId, null, listOf())

        given(jwtProvider.getUserIdFromToken(anyString())).willReturn(userId)
    }

    @Test
    fun `kakaoLogin_성공`() {
        val request = LoginRequest("auth-code")
        given(authService.kakaoLogin(anyString())).willReturn(loginResponse)

        mockMvc.perform(
            post("/api/auth/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("access-token"))
            .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
    }

    @Test
    fun `kakaoLogin_코드빈값_400`() {
        val request = LoginRequest("")

        mockMvc.perform(
            post("/api/auth/kakao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `reissueToken_성공`() {
        val request = TokenReissueRequest("refresh-token")
        given(authService.reissueToken(anyNonNull())).willReturn(loginResponse)

        mockMvc.perform(
            post("/api/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("access-token"))
    }

    @Test
    fun `reissueToken_토큰빈값_400`() {
        val request = TokenReissueRequest("")

        mockMvc.perform(
            post("/api/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `reissueToken_유효하지않은토큰_401`() {
        val request = TokenReissueRequest("invalid-token")
        given(authService.reissueToken(anyNonNull()))
            .willThrow(CustomException(ErrorCode.INVALID_TOKEN))

        mockMvc.perform(
            post("/api/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    fun `logout_성공`() {
        willDoNothing().given(authService).logout(anyNonNull())

        mockMvc.perform(
            delete("/api/auth/logout")
                .with(authentication(auth))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }
}
