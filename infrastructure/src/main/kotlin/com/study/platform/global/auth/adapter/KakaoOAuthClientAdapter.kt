package com.study.platform.global.auth.adapter

import com.study.platform.domain.user.dto.response.OAuthUserInfo
import com.study.platform.global.auth.port.OAuthClient
import com.study.platform.global.oauth.kakao.KakaoOAuthClient
import org.springframework.stereotype.Component

@Component
class KakaoOAuthClientAdapter(
    private val kakaoOAuthClient: KakaoOAuthClient
) : OAuthClient {

    override fun getUserInfo(code: String): OAuthUserInfo {
        val token = kakaoOAuthClient.getToken(code)
        val payload = kakaoOAuthClient.parseIdToken(token.idToken)
        return OAuthUserInfo(payload.sub, payload.nickname, payload.email)
    }
}
