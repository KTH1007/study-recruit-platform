package com.study.platform.domain.user.application;

import com.study.platform.domain.user.dto.request.TokenReissueRequest;
import com.study.platform.domain.user.dto.response.LoginResponse;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import com.study.platform.global.jwt.JwtProvider;
import com.study.platform.global.oauth.kakao.KakaoOAuthClient;
import com.study.platform.global.oauth.kakao.dto.KakaoIdTokenPayload;
import com.study.platform.global.oauth.kakao.dto.KakaoTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;
    private final KakaoOAuthClient kakaoOAuthClient;

    @Transactional
    public LoginResponse kakaoLogin(String code) {
        KakaoTokenResponse kakaoToken = kakaoOAuthClient.getToken(code);
        KakaoIdTokenPayload payload = kakaoOAuthClient.parseIdToken(kakaoToken.idToken());

        User user = userRepository.findByKakaoId(payload.sub())
                .orElseGet(() -> userRepository.save(User.create(
                        payload.sub(),
                        payload.nickname(),
                        payload.email()
                )));

        return generateTokens(user.getId());
    }

    public LoginResponse reissueToken(TokenReissueRequest request) {
        jwtProvider.validateToken(request.refreshToken());
        UUID userId = jwtProvider.getUserIdFromToken(request.refreshToken());
        String savedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (!request.refreshToken().equals(savedToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return generateTokens(userId);
    }

    private LoginResponse generateTokens(UUID userId) {
        String accessToken = jwtProvider.generateAccessToken(userId);
        String refreshToken = jwtProvider.generateRefreshToken(userId);
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_TTL);
        return LoginResponse.of(accessToken, refreshToken);
    }
}
