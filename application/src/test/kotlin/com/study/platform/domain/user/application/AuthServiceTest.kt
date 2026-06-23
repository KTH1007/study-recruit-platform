package com.study.platform.domain.user.application

import com.study.platform.domain.user.dto.request.TokenReissueRequest
import com.study.platform.domain.user.dto.response.LoginResponse
import com.study.platform.domain.user.model.User
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.support.fake.FakeOAuthClient
import com.study.platform.support.fake.FakeTokenManager
import com.study.platform.support.fake.FakeTokenRepository
import com.study.platform.support.fake.FakeUserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.UUID

class AuthServiceTest {

    private lateinit var userRepository: FakeUserRepository
    private lateinit var tokenManager: FakeTokenManager
    private lateinit var tokenRepository: FakeTokenRepository
    private lateinit var oAuthClient: FakeOAuthClient
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        userRepository = FakeUserRepository()
        tokenManager = FakeTokenManager()
        tokenRepository = FakeTokenRepository()
        oAuthClient = FakeOAuthClient()
        authService = AuthService(userRepository, tokenManager, tokenRepository, oAuthClient)
    }

    @Test
    fun `kakaoLogin_신규유저_회원가입후로그인`() {
        // given
        oAuthClient.register("auth-code", "kakao-123", "테스트유저", "test@test.com")

        // when
        val response: LoginResponse = authService.kakaoLogin("auth-code")

        // then
        assertThat(response.accessToken).isNotBlank()
        assertThat(response.refreshToken).isNotBlank()
        assertThat(userRepository.findByKakaoId("kakao-123")).isNotNull()
    }

    @Test
    fun `kakaoLogin_기존유저_로그인`() {
        // given
        oAuthClient.register("auth-code", "kakao-123", "테스트유저", "test@test.com")
        userRepository.save(User.create("kakao-123", "테스트유저", "test@test.com"))

        // when
        val response: LoginResponse = authService.kakaoLogin("auth-code")

        // then
        assertThat(response.accessToken).isNotBlank()
        assertThat(userRepository.findAllByNicknameIn(listOf("테스트유저"))).hasSize(1)
    }

    @Test
    fun `kakaoLogin_닉네임중복_suffix추가후저장`() {
        // given
        oAuthClient.register("auth-code", "kakao-123", "테스트유저", "test@test.com")
        userRepository.save(User.create("kakao-456", "테스트유저", "other@test.com"))

        // when
        authService.kakaoLogin("auth-code")

        // then
        val saved = userRepository.findByKakaoId("kakao-123")
        assertThat(saved?.nickname).startsWith("테스트유저_")
    }

    @Test
    fun `reissueToken_성공`() {
        // given
        oAuthClient.register("auth-code", "kakao-123", "테스트유저", "test@test.com")
        val firstLogin = authService.kakaoLogin("auth-code")
        val refreshToken = firstLogin.refreshToken

        // when
        val response: LoginResponse = authService.reissueToken(TokenReissueRequest(refreshToken))

        // then
        assertThat(response.accessToken).isNotBlank()
        assertThat(response.refreshToken).isNotBlank()
    }

    @Test
    fun `reissueToken_저장된토큰불일치_예외발생`() {
        // given
        val userId = UUID.randomUUID()
        val refreshToken = tokenManager.generateRefreshToken(userId)
        tokenRepository.save("refreshToken:$userId", "other-token", Duration.ofDays(7))

        // when & then
        assertThatThrownBy { authService.reissueToken(TokenReissueRequest(refreshToken)) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN)
    }

    @Test
    fun `reissueToken_유효하지않은토큰_예외발생`() {
        // given
        val invalidToken = "invalid-token"

        // when & then
        assertThatThrownBy { authService.reissueToken(TokenReissueRequest(invalidToken)) }
            .isInstanceOf(CustomException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN)
    }

    @Test
    fun `logout_성공`() {
        // given
        oAuthClient.register("auth-code", "kakao-123", "테스트유저", "test@test.com")
        val loginResponse = authService.kakaoLogin("auth-code")
        val userId = tokenManager.getUserIdFromToken(loginResponse.refreshToken)

        // when
        authService.logout(userId)

        // then
        assertThat(tokenRepository.find("refreshToken:$userId")).isNull()
    }
}
