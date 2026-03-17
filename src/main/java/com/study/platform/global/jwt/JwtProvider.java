package com.study.platform.global.jwt;

import com.study.platform.global.exception.CustomException;
import com.study.platform.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtProvider {

    private static final String USER_ID_CLAIM = "userId";

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration.access}") long accessTokenExpiration,
            @Value("${jwt.expiration.refresh}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(UUID userId) {
        return buildToken(userId, accessTokenExpiration);
    }

    public String generateRefreshToken(UUID userId) {
        return buildToken(userId, refreshTokenExpiration);
    }

    public UUID getUserIdFromToken(String token) {
        String userIdStr = parseClaims(token).get(USER_ID_CLAIM, String.class);
        return UUID.fromString(userIdStr);
    }

    public void validateToken(String token) {
        try {
            parseClaims(token);
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token");
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    private String buildToken(UUID userId, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .claim(USER_ID_CLAIM, userId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
