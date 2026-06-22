package com.study.platform.domain.user.application

import com.study.platform.domain.user.dto.request.TokenReissueRequest
import com.study.platform.domain.user.dto.response.LoginResponse
import com.study.platform.domain.user.model.User
import com.study.platform.domain.user.model.UserRepository
import com.study.platform.global.auth.port.OAuthClient
import com.study.platform.global.auth.port.TokenManager
import com.study.platform.global.auth.port.TokenRepository
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.UUID

@Service
@Transactional(readOnly = true)
class AuthService(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    private val tokenRepository: TokenRepository,
    private val oAuthClient: OAuthClient
) {
    companion object {
        private const val REFRESH_TOKEN_PREFIX = "refreshToken:"
        private val REFRESH_TOKEN_TTL: Duration = Duration.ofDays(7)
    }

    @Transactional
    fun kakaoLogin(code: String): LoginResponse {
        val userInfo = oAuthClient.getUserInfo(code)

        val user = userRepository.findByKakaoId(userInfo.oauthId)
            ?: userRepository.save(
                User.create(
                    userInfo.oauthId,
                    resolveUniqueNickname(userInfo.nickname),
                    userInfo.email
                )
            )

        return generateTokens(user.id!!)
    }

    fun reissueToken(request: TokenReissueRequest): LoginResponse {
        tokenManager.validateToken(request.refreshToken)
        val userId = tokenManager.getUserIdFromToken(request.refreshToken)
        val savedToken = tokenRepository.find("$REFRESH_TOKEN_PREFIX$userId")
            ?: throw CustomException(ErrorCode.INVALID_TOKEN)
        if (request.refreshToken != savedToken) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
        return generateTokens(userId)
    }

    fun logout(userId: UUID) {
        tokenRepository.delete("$REFRESH_TOKEN_PREFIX$userId")
    }

    private fun generateTokens(userId: UUID): LoginResponse {
        val accessToken = tokenManager.generateAccessToken(userId)
        val refreshToken = tokenManager.generateRefreshToken(userId)
        tokenRepository.save("$REFRESH_TOKEN_PREFIX$userId", refreshToken, REFRESH_TOKEN_TTL)
        return LoginResponse.of(accessToken, refreshToken)
    }

    private fun resolveUniqueNickname(nickname: String): String {
        if (userRepository.findByNickname(nickname) == null) {
            return nickname
        }
        var suffix = 1
        while (userRepository.findByNickname("${nickname}_$suffix") != null) {
            suffix++
        }
        return "${nickname}_$suffix"
    }
}
