package com.bootcamp.paymentdemo.domain.member.repository;

import com.bootcamp.paymentdemo.domain.member.entity.AccessTokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AccessTokenBlacklistRepository extends JpaRepository<AccessTokenBlacklist, Long> {

    boolean existsByJtiAndExpiresAtAfter(String jti, LocalDateTime now);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
