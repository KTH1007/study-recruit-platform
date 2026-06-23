package com.study.platform.global.jwt

import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.expiration.access}") private val accessTokenExpiration: Long,
    @Value("\${jwt.expiration.refresh}") private val refreshTokenExpiration: Long
) {

    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
    private val log = LoggerFactory.getLogger(JwtProvider::class.java)!!

    companion object {
        private const val USER_ID_CLAIM = "userId"
    }

    fun generateAccessToken(userId: UUID): String =
        buildToken(userId, accessTokenExpiration)

    fun generateRefreshToken(userId: UUID): String =
        buildToken(userId, refreshTokenExpiration)

    fun getUserIdFromToken(token: String): UUID {
        val userIdStr = parseClaims(token).get(USER_ID_CLAIM, String::class.java)
        return UUID.fromString(userIdStr)
    }

    fun validateToken(token: String) {
        try {
            parseClaims(token)
        } catch (e: ExpiredJwtException) {
            log.warn("Expired JWT token")
            throw CustomException(ErrorCode.EXPIRED_TOKEN)
        } catch (e: JwtException) {
            log.warn("Invalid JWT token")
            throw CustomException(ErrorCode.INVALID_TOKEN)
        } catch (e: IllegalArgumentException) {
            log.warn("Invalid JWT token")
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
    }

    private fun buildToken(userId: UUID, expiration: Long): String {
        val now = Date()
        return Jwts.builder()
            .claim(USER_ID_CLAIM, userId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + expiration))
            .signWith(secretKey)
            .compact()
    }

    private fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
