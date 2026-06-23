package com.study.platform.domain.user.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class LoginResponse(

    @field:Schema(description = "액세스 토큰")
    val accessToken: String,

    @field:Schema(description = "리프레시 토큰")
    val refreshToken: String
) {
    companion object {
        fun of(accessToken: String, refreshToken: String): LoginResponse =
            LoginResponse(accessToken, refreshToken)
    }
}
