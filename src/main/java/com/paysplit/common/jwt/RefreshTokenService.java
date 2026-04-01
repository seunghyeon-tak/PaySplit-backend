package com.paysplit.common.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    // refresh token 저장
    public void saveRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                7,
                TimeUnit.DAYS
        );
    }

    // refresh token 조회
    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
    }

    // refresh token 삭제 (로그아웃)
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }
}
