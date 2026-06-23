package com.study.platform.global.auth.port

import java.util.UUID

interface TokenManager {
    fun generateAccessToken(userId: UUID): String
    fun generateRefreshToken(userId: UUID): String
    fun validateToken(token: String)
    fun getUserIdFromToken(token: String): UUID
}
