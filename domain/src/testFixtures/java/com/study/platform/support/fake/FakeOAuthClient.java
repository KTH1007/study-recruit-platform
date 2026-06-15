package com.study.platform.support.fake;

import com.study.platform.domain.user.dto.response.OAuthUserInfo;
import com.study.platform.global.auth.port.OAuthClient;

import java.util.HashMap;
import java.util.Map;

public class FakeOAuthClient implements OAuthClient {

    private final Map<String, OAuthUserInfo> codeToUserInfo = new HashMap<>();

    public void register(String code, String oauthId, String nickname, String email) {
        codeToUserInfo.put(code, new OAuthUserInfo(oauthId, nickname, email));
    }

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        return codeToUserInfo.get(code);
    }
}
