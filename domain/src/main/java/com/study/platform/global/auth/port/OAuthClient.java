package com.study.platform.global.auth.port;

import com.study.platform.domain.user.dto.response.OAuthUserInfo;

public interface OAuthClient {
    OAuthUserInfo getUserInfo(String code);
}