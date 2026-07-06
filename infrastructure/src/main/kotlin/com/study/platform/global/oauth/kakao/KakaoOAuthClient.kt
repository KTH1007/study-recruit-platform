package com.study.platform.global.oauth.kakao

import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.oauth.kakao.dto.KakaoIdTokenPayload
import com.study.platform.global.oauth.kakao.dto.KakaoTokenResponse
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.ProtectedHeader
import io.jsonwebtoken.security.JwkSet
import io.jsonwebtoken.security.Jwks
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import java.security.Key
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Component
class KakaoOAuthClient(
    @Value("\${kakao.client-id}") private val clientId: String,
    @Value("\${kakao.client-secret}") private val clientSecret: String,
    @Value("\${kakao.redirect-uri}") private val redirectUri: String
) {

    private val restClient: RestClient = RestClient.create()
    private val log = LoggerFactory.getLogger(KakaoOAuthClient::class.java)!!

    private val jwkSetLock = ReentrantLock()

    @Volatile
    private var cachedJwkSet: JwkSet? = null

    companion object {
        private const val KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token"
        private const val KAKAO_JWKS_URL = "https://kauth.kakao.com/.well-known/jwks.json"
        private const val KAKAO_ISSUER = "https://kauth.kakao.com"
    }

    fun getToken(code: String): KakaoTokenResponse {
        val params = LinkedMultiValueMap<String, String>()
        params.add("grant_type", "authorization_code")
        params.add("client_id", clientId)
        params.add("client_secret", clientSecret)
        params.add("redirect_uri", redirectUri)
        params.add("code", code)

        return restClient.post()
            .uri(KAKAO_TOKEN_URL)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(params)
            .retrieve()
            .body(KakaoTokenResponse::class.java)!!
    }

    fun parseIdToken(idToken: String): KakaoIdTokenPayload {
        try {
            val claims = Jwts.parser()
                .keyLocator { header -> resolveSigningKey(header as ProtectedHeader) }
                .requireIssuer(KAKAO_ISSUER)
                .requireAudience(clientId)
                .build()
                .parseSignedClaims(idToken)
                .payload

            return KakaoIdTokenPayload(
                sub = claims.subject,
                email = claims["email"] as? String ?: "",
                nickname = claims["nickname"] as? String ?: ""
            )
        } catch (e: JwtException) {
            log.error("id_token 검증 실패: {}", e.message)
            throw CustomException(ErrorCode.INVALID_TOKEN)
        } catch (e: IllegalArgumentException) {
            log.error("id_token 검증 실패: {}", e.message)
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
    }

    private fun resolveSigningKey(header: ProtectedHeader): Key {
        val kid = header.keyId
            ?: throw IllegalArgumentException("id_token 헤더에 kid가 없습니다")

        val jwkSet = cachedJwkSet ?: fetchAndCacheJwkSet()
        val jwk = jwkSet.getKeys().find { it.id == kid }
            ?: fetchAndCacheJwkSet().getKeys().find { it.id == kid }
            ?: throw IllegalArgumentException("일치하는 JWK를 찾을 수 없습니다. kid=$kid")
        return jwk.toKey()
    }

    private fun fetchAndCacheJwkSet(): JwkSet = jwkSetLock.withLock {
        val json = restClient.get().uri(KAKAO_JWKS_URL).retrieve().body(String::class.java)
            ?: throw IllegalStateException("Kakao JWKS 응답이 비어 있습니다")
        Jwks.setParser().build().parse(json).also { cachedJwkSet = it }
    }
}
