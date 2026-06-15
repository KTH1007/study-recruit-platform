package com.study.platform.domain.user.application;

import com.study.platform.domain.user.dto.request.TokenReissueRequest;
import com.study.platform.domain.user.dto.response.LoginResponse;
import com.study.platform.domain.user.dto.response.OAuthUserInfo;
import com.study.platform.domain.user.model.User;
import com.study.platform.domain.user.model.UserRepository;
import com.study.platform.global.auth.port.OAuthClient;
import com.study.platform.global.auth.port.TokenManager;
import com.study.platform.global.auth.port.TokenRepository;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
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
    private final TokenManager tokenManager;
    private final TokenRepository tokenRepository;
    private final OAuthClient oAuthClient;

    @Transactional
    public LoginResponse kakaoLogin(String code) {
        OAuthUserInfo userInfo = oAuthClient.getUserInfo(code);

        User user = userRepository.findByKakaoId(userInfo.oauthId())
                .orElseGet(() -> userRepository.save(User.create(
                        userInfo.oauthId(),
                        resolveUniqueNickname(userInfo.nickname()),
                        userInfo.email()
                )));

        return generateTokens(user.getId());
    }

    public LoginResponse reissueToken(TokenReissueRequest request) {
        tokenManager.validateToken(request.refreshToken());
        UUID userId = tokenManager.getUserIdFromToken(request.refreshToken());
        String savedToken = tokenRepository.find(REFRESH_TOKEN_PREFIX + userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));
        if (!request.refreshToken().equals(savedToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return generateTokens(userId);
    }

    public void logout(UUID userId) {
        tokenRepository.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    private LoginResponse generateTokens(UUID userId) {
        String accessToken = tokenManager.generateAccessToken(userId);
        String refreshToken = tokenManager.generateRefreshToken(userId);
        tokenRepository.save(REFRESH_TOKEN_PREFIX + userId, refreshToken, REFRESH_TOKEN_TTL);
        return LoginResponse.of(accessToken, refreshToken);
    }

    private String resolveUniqueNickname(String nickname) {
        if (userRepository.findByNickname(nickname).isEmpty()) {
            return nickname;
        }
        int suffix = 1;
        while (userRepository.findByNickname(nickname + "_" + suffix).isPresent()) {
            suffix++;
        }
        return nickname + "_" + suffix;
    }
}
