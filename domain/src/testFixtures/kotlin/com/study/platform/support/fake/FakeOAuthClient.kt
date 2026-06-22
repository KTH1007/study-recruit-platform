package com.study.platform.support.fake

import com.study.platform.domain.user.dto.response.OAuthUserInfo
import com.study.platform.global.auth.port.OAuthClient

class FakeOAuthClient : OAuthClient {

    private val codeToUserInfo: MutableMap<String, OAuthUserInfo> = HashMap()

    fun register(code: String, oauthId: String, nickname: String, email: String) {
        codeToUserInfo[code] = OAuthUserInfo(oauthId, nickname, email)
    }

    override fun getUserInfo(code: String): OAuthUserInfo = codeToUserInfo[code]!!
}
