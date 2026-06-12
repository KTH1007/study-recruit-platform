package com.study.platform.global.auth.adapter;

import com.study.platform.domain.user.dto.response.OAuthUserInfo;
import com.study.platform.global.auth.port.OAuthClient;
import com.study.platform.global.oauth.kakao.KakaoOAuthClient;
import com.study.platform.global.oauth.kakao.dto.KakaoIdTokenPayload;
import com.study.platform.global.oauth.kakao.dto.KakaoTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClientAdapter implements OAuthClient {

    private final KakaoOAuthClient kakaoOAuthClient;

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        KakaoTokenResponse token = kakaoOAuthClient.getToken(code);
        KakaoIdTokenPayload payload = kakaoOAuthClient.parseIdToken(token.idToken());
        return new OAuthUserInfo(payload.sub(), payload.nickname(), payload.email());
    }
}
