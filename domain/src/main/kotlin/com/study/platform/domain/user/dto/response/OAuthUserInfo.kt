package com.study.platform.domain.user.dto.response

data class OAuthUserInfo(
    val oauthId: String,
    val nickname: String,
    val email: String
)
