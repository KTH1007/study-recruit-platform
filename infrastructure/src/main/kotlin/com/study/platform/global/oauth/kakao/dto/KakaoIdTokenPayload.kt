package com.study.platform.global.oauth.kakao.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoIdTokenPayload(
    val sub: String,
    val email: String,
    @JsonProperty("nickname")
    val nickname: String
)
