package com.ecommerce.modules.identity.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_REFRESH_TOKEN_PREFIX = "user_refresh:";
    private static final String BLACKLIST_TOKEN_PREFIX = "token_blacklist:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${app.jwt.refresh-token-expiration-ms:2592000000}")
    private long refreshTokenExpirationMs;

    public String createRefreshToken(Long userId) {
        byte[] randomBytes = new byte[48];
        SECURE_RANDOM.nextBytes(randomBytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String userKey = USER_REFRESH_TOKEN_PREFIX + userId;

        // Lưu refresh_token -> userId
        redisTemplate.opsForValue().set(tokenKey, String.valueOf(userId), refreshTokenExpirationMs, TimeUnit.MILLISECONDS);
        // Lưu user -> refresh_token
        redisTemplate.opsForValue().set(userKey, refreshToken, refreshTokenExpirationMs, TimeUnit.MILLISECONDS);

        return refreshToken;
    }

    public Long validateRefreshToken(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        Object userIdObj = redisTemplate.opsForValue().get(tokenKey);
        if (userIdObj == null) {
            return null;
        }
        return Long.parseLong(userIdObj.toString());
    }

    public void revokeRefreshToken(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        Object userIdObj = redisTemplate.opsForValue().get(tokenKey);
        if (userIdObj != null) {
            String userKey = USER_REFRESH_TOKEN_PREFIX + userIdObj.toString();
            redisTemplate.delete(userKey);
        }
        redisTemplate.delete(tokenKey);
    }

    public void revokeAllUserRefreshTokens(Long userId) {
        String userKey = USER_REFRESH_TOKEN_PREFIX + userId;
        Object refreshTokenObj = redisTemplate.opsForValue().get(userKey);
        if (refreshTokenObj != null) {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshTokenObj.toString();
            redisTemplate.delete(tokenKey);
        }
        redisTemplate.delete(userKey);
    }

    public void blacklistAccessToken(String accessToken, long remainingTtlMs) {
        if (remainingTtlMs > 0) {
            String key = BLACKLIST_TOKEN_PREFIX + accessToken;
            redisTemplate.opsForValue().set(key, "revoked", remainingTtlMs, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isAccessTokenBlacklisted(String accessToken) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
