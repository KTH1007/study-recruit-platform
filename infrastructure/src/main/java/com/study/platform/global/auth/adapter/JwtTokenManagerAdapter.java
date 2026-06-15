package com.study.platform.global.auth.adapter;

import com.study.platform.global.auth.port.TokenManager;
import com.study.platform.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenManagerAdapter implements TokenManager {

    private final JwtProvider jwtProvider;

    @Override
    public String generateAccessToken(UUID userId) {
        return jwtProvider.generateAccessToken(userId);
    }

    @Override
    public String generateRefreshToken(UUID userId) {
        return jwtProvider.generateRefreshToken(userId);
    }

    @Override
    public void validateToken(String token) {
        jwtProvider.validateToken(token);
    }

    @Override
    public UUID getUserIdFromToken(String token) {
        return jwtProvider.getUserIdFromToken(token);
    }
}
