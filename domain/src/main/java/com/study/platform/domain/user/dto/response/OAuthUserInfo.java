package com.study.platform.domain.user.dto.response;

public record OAuthUserInfo(
        String oauthId,
        String nickname,
        String email
) {}
