package com.axis.nachimbal.domain.security.jwt;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("refreshToken")
public class RefreshToken {

    @Id
    private Long userId; // key: refreshToken:{userId}

    private String token;

    @TimeToLive
    private Long expiration; // 초 단위 — Redis TTL

    public RefreshToken(Long userId, String token, Long expiration) {
        this.userId = userId;
        this.token = token;
        this.expiration = expiration;
    }

    public void updateToken(String token) {
        this.token = token;
    }
}