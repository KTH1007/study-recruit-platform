package com.study.platform.global.oauth.kakao

import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import com.study.platform.global.exception.CustomException
import com.study.platform.global.exception.ErrorCode
import com.study.platform.global.oauth.kakao.dto.KakaoIdTokenPayload
import com.study.platform.global.oauth.kakao.dto.KakaoTokenResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import java.util.Base64

@Component
class KakaoOAuthClient(
    @Value("\${kakao.client-id}") private val clientId: String,
    @Value("\${kakao.client-secret}") private val clientSecret: String,
    @Value("\${kakao.redirect-uri}") private val redirectUri: String
) {

    private val restClient: RestClient = RestClient.create()
    private val objectMapper: ObjectMapper = ObjectMapper()
    private val log = LoggerFactory.getLogger(KakaoOAuthClient::class.java)!!

    companion object {
        private const val KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token"
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
            val parts = idToken.split(".")
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            return objectMapper.readValue(payload, KakaoIdTokenPayload::class.java)
        } catch (e: JacksonException) {
            log.error("id_token parse error: {}", e.message)
            throw CustomException(ErrorCode.INVALID_TOKEN)
        } catch (e: IllegalArgumentException) {
            log.error("id_token parse error: {}", e.message)
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
    }
}
