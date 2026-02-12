package com.bootcamp.paymentdemo.domain.member.scheduler;

import com.bootcamp.paymentdemo.domain.member.service.AccessTokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessBlacklistCleanupScheduler {

    private final AccessTokenBlacklistService accessTokenBlacklistService;

    @Scheduled(cron = "0 0 * * * *") // 매 시간
    public void cleanup() {
        accessTokenBlacklistService.cleanupExpired();
    }
}
