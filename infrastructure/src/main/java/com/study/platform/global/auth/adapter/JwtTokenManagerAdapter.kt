package com.study.platform.global.auth.adapter

import com.study.platform.global.auth.port.TokenManager
import com.study.platform.global.jwt.JwtProvider
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class JwtTokenManagerAdapter(
    private val jwtProvider: JwtProvider
) : TokenManager {

    override fun generateAccessToken(userId: UUID): String =
        jwtProvider.generateAccessToken(userId)

    override fun generateRefreshToken(userId: UUID): String =
        jwtProvider.generateRefreshToken(userId)

    override fun validateToken(token: String) {
        jwtProvider.validateToken(token)
    }

    override fun getUserIdFromToken(token: String): UUID =
        jwtProvider.getUserIdFromToken(token)
}
