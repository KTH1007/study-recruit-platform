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
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
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
    private val log = LoggerFactory.getLogger(AuthService::class.java)

    companion object {
        private const val REFRESH_TOKEN_PREFIX = "refreshToken:"
        private val REFRESH_TOKEN_TTL: Duration = Duration.ofDays(7)
        private const val MAX_CREATE_USER_ATTEMPTS = 2
    }

    @Transactional
    fun kakaoLogin(code: String): LoginResponse {
        val userInfo = oAuthClient.getUserInfo(code)

        val existingUser = userRepository.findByKakaoId(userInfo.oauthId)
        val user = existingUser ?: createUser(userInfo.oauthId, userInfo.nickname, userInfo.email)

        return try {
            generateTokens(user.id!!)
        } catch (e: Exception) {
            // createUser는 REQUIRES_NEW로 이미 독립 커밋되므로, 여기서 실패해도 유저는 남는다.
            // 다음 로그인 시 findByKakaoId로 정상 복구되므로 데이터 유실은 아니지만 운영 관측을 위해 남긴다.
            if (existingUser == null) {
                log.error("카카오 로그인 - 유저 생성 후 토큰 발급 실패. userId={}, 재로그인 시 정상 처리됩니다.", user.id, e)
            }
            throw e
        }
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

    private fun createUser(kakaoId: String, nickname: String, email: String): User {
        repeat(MAX_CREATE_USER_ATTEMPTS) {
            try {
                return userRepository.saveNew(User.create(kakaoId, resolveUniqueNickname(nickname), email))
            } catch (e: DataIntegrityViolationException) {
                // kakaoId 충돌(동시 최초 로그인)이면 방금 생성된 유저를 반환하고,
                // 닉네임 충돌(다른 사용자와의 경쟁)이면 닉네임을 다시 채번해 한 번 더 시도한다.
                userRepository.findByKakaoId(kakaoId)?.let { return it }
            }
        }
        throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)
    }

    private fun generateTokens(userId: UUID): LoginResponse {
        val accessToken = tokenManager.generateAccessToken(userId)
        val refreshToken = tokenManager.generateRefreshToken(userId)
        tokenRepository.save("$REFRESH_TOKEN_PREFIX$userId", refreshToken, REFRESH_TOKEN_TTL)
        return LoginResponse.of(accessToken, refreshToken)
    }

    private fun resolveUniqueNickname(nickname: String): String {
        if (userRepository.findByNickname(nickname) == null) return nickname
        return "${nickname}_${UUID.randomUUID().toString().take(8)}"
    }
}
