package com.study.platform.global.oauth.kakao.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoIdTokenPayload @JsonCreator constructor(
    @JsonProperty("sub")
    val sub: String,
    @JsonProperty("email")
    val email: String,
    @JsonProperty("nickname")
    val nickname: String
)
