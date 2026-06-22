package com.study.platform.global.auth.port

import com.study.platform.domain.user.dto.response.OAuthUserInfo

interface OAuthClient {
    fun getUserInfo(code: String): OAuthUserInfo
}
