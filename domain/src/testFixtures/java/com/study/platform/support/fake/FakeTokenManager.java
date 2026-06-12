package com.study.platform.support.fake;

import com.study.platform.global.auth.port.TokenManager;
import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FakeTokenManager implements TokenManager {

    private final Map<String, UUID> tokenStore = new HashMap<>();
    private final Map<UUID, String> invalidTokens = new HashMap<>();

    @Override
    public String generateAccessToken(UUID userId) {
        String token = "access-" + userId;
        tokenStore.put(token, userId);
        return token;
    }

    @Override
    public String generateRefreshToken(UUID userId) {
        String token = "refresh-" + userId;
        tokenStore.put(token, userId);
        return token;
    }

    @Override
    public void validateToken(String token) {
        if (!tokenStore.containsKey(token)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    @Override
    public UUID getUserIdFromToken(String token) {
        if (!tokenStore.containsKey(token)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return tokenStore.get(token);
    }
}
