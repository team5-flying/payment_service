package com.bootcamp.paymentdemo.domain.subscription.entity;

import com.bootcamp.paymentdemo.common.entity.Base;
import com.bootcamp.paymentdemo.domain.member.entity.Member;
import com.bootcamp.paymentdemo.domain.payment.entity.PaymentMethod;
import com.bootcamp.paymentdemo.domain.plan.entity.Plan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "subscriptions")
@NoArgsConstructor
public class Subscription extends Base {

    // 체험기간 정하기
    private static final int DEFAULT_TRIAL_PERIOD_DAYS = 7;

    @Id
    @Column(name = "subscription_id")
    private String subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status; // TRIALING, ACTIVE, CANCELLED 등

    @Column(nullable = false)
    private LocalDateTime currentPeriodEnd;

    private LocalDateTime trialEnd;
    private String reason;
    private String action;
    private LocalDateTime canceledAt;

    @Column(nullable = false)
    private boolean success;

    public static Subscription createSubscription(String id, Plan plan, Member member, PaymentMethod paymentMethod, Long amount) {
        Subscription subscription = new Subscription();
        subscription.subscriptionId = id;
        subscription.plan = plan;
        subscription.member = member;
        subscription.paymentMethod = paymentMethod;
        subscription.amount = amount;
        subscription.success = true; // 생성 성공으로 초기화
        subscription.action = "CREATE"; // 초기 액션 기록

        LocalDateTime now = LocalDateTime.now();
        
        // 체험 기간 적용
        if (DEFAULT_TRIAL_PERIOD_DAYS > 0) {
            subscription.status = SubscriptionStatus.TRIALING;
            subscription.trialEnd = now.plusDays(DEFAULT_TRIAL_PERIOD_DAYS);
            subscription.currentPeriodEnd = subscription.trialEnd;
        } else {
            subscription.status = SubscriptionStatus.ACTIVE;
            switch (plan.getBillingCycle()) {
                case MONTHLY -> subscription.currentPeriodEnd = now.plusMonths(1);
                case QUARTERLY -> subscription.currentPeriodEnd = now.plusMonths(3);
                case ANNUAL -> subscription.currentPeriodEnd = now.plusYears(1);
            }
        }

        return subscription;
    }

    // 결제성공 시 주기 갱신
    public void renewSubscription() {
        this.status = SubscriptionStatus.ACTIVE;

        switch (this.plan.getBillingCycle()) {
            case MONTHLY -> this.currentPeriodEnd = this.currentPeriodEnd.plusMonths(1);
            case QUARTERLY -> this.currentPeriodEnd = this.currentPeriodEnd.plusMonths(3);
            case ANNUAL -> this.currentPeriodEnd = this.currentPeriodEnd.plusYears(1);
        }
    }

    public void expireSubscription() {
        this.status = SubscriptionStatus.PAST_DUE;
    }

    public void cancel(String reason) {
        this.status = SubscriptionStatus.CANCELED;
        this.reason = reason;
        this.action = "CANCEL";
        this.canceledAt = LocalDateTime.now();
    }
}
