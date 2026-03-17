package com.study.platform.global.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoIdTokenPayload(

        String sub,

        String email,

        @JsonProperty("nickname")
        String nickname
) {}