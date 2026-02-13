package com.bootcamp.paymentdemo.domain.member.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "access_token_blacklist")
public class AccessTokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String jti;

    // 토큰 만료 시간까지만 블랙리스트 의미가 있음
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static AccessTokenBlacklist register(String jti, LocalDateTime expiresAt) {
        AccessTokenBlacklist accessTokenBlacklist = new AccessTokenBlacklist();
        accessTokenBlacklist.jti = jti;
        accessTokenBlacklist.expiresAt = expiresAt;
        accessTokenBlacklist.createdAt = LocalDateTime.now();
        return accessTokenBlacklist;
    }
}
