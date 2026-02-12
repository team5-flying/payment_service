package com.bootcamp.paymentdemo.domain.member.service;

import com.bootcamp.paymentdemo.domain.member.entity.AccessTokenBlacklist;
import com.bootcamp.paymentdemo.domain.member.repository.AccessTokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccessTokenBlacklistService {

    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String jti) {
        return accessTokenBlacklistRepository.existsByJtiAndExpiresAtAfter(jti, LocalDateTime.now());
    }

    @Transactional
    public void blacklist(String jti, LocalDateTime expiresAt) {
        accessTokenBlacklistRepository.save(AccessTokenBlacklist.register(jti, expiresAt));
    }

    @Transactional
    public void cleanupExpired() {
        accessTokenBlacklistRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
