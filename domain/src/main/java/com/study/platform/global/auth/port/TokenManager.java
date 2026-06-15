package com.study.platform.global.auth.port;

import java.util.UUID;

public interface TokenManager {
    String generateAccessToken(UUID userId);
    String generateRefreshToken(UUID userId);
    void validateToken(String token);
    UUID getUserIdFromToken(String token);
}
