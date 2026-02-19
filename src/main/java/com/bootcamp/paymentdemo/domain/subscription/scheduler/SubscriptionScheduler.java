package com.bootcamp.paymentdemo.domain.subscription.scheduler;

import com.bootcamp.paymentdemo.domain.subscription.dto.CreateBillingRequest;
import com.bootcamp.paymentdemo.domain.subscription.entity.Subscription;
import com.bootcamp.paymentdemo.domain.subscription.entity.SubscriptionStatus;
import com.bootcamp.paymentdemo.domain.subscription.repository.SubscriptionRepository;
import com.bootcamp.paymentdemo.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

//    @Scheduled(cron = "0 0 4 * * *") 테스트를 위해 주석처리하고 3분마다 스케줄러 작동
    @Scheduled(cron = "0 */3 * * * *")
    public void processScheduledBillings() {
        log.info("정기 결제 스케줄러 시작");
        LocalDateTime now = LocalDateTime.now();

        // 결제 대상 조회
        List<Subscription> subscriptions = subscriptionRepository.findAllByStatusInAndCurrentPeriodEndBefore(List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE, SubscriptionStatus.TRIALING), now);

        for (Subscription sub : subscriptions) {
            try {
                subscriptionService.createBilling(sub.getSubscriptionId(), new CreateBillingRequest(sub.getCurrentPeriodEnd(), now));
            } catch (Exception e) {
                log.error("구독 결제 처리 실패, 구독ID: {}", sub.getSubscriptionId(), e);
            }
        }
        log.info("정기 결제 스케줄러 종료, 처리 건수: {}", subscriptions.size());
    }
}
