package com.study.platform.support.fake

import com.study.platform.global.auth.port.TokenManager
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import java.util.UUID

class FakeTokenManager : TokenManager {

    private val tokenStore: MutableMap<String, UUID> = HashMap()

    override fun generateAccessToken(userId: UUID): String {
        val token = "access-$userId"
        tokenStore[token] = userId
        return token
    }

    override fun generateRefreshToken(userId: UUID): String {
        val token = "refresh-$userId"
        tokenStore[token] = userId
        return token
    }

    override fun validateToken(token: String) {
        if (!tokenStore.containsKey(token)) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
    }

    override fun getUserIdFromToken(token: String): UUID {
        if (!tokenStore.containsKey(token)) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
        return tokenStore[token]!!
    }
}
