package com.bootcamp.paymentdemo.domain.subscription.entity;

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
public class Subscription {

    @Id
    @Column(name = "subscription_id")
    private String subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status; // TRIALING, ACTIVE, CANCELLED 등

    @Column(nullable = false)
    private LocalDateTime currentPeriodEnd;


    private String reason;

    private String action;

    @Column(nullable = false)
    private boolean success;

    public static Subscription createSubscription(String id, Plan plan, Member member, PaymentMethod paymentMethod, Long amount) {
        Subscription subscription = new Subscription();
        subscription.subscriptionId = id;
        subscription.plan = plan;
        subscription.member = member;
        subscription.paymentMethod = paymentMethod;
        subscription.status = SubscriptionStatus.TRIALING; // 생성시 TRIALING으로 설정
        subscription.amount = amount;
        subscription.success = true; // 생성 성공으로 초기화
        subscription.action = "CREATE"; // 초기 액션 기록

        LocalDateTime now = LocalDateTime.now();
        switch (plan.getBillingCycle()) {
            case MONTHLY -> subscription.currentPeriodEnd = now.plusMonths(1);
            case QUARTERLY -> subscription.currentPeriodEnd = now.plusMonths(3);
            case ANNUAL -> subscription.currentPeriodEnd = now.plusYears(1);
        }

        return subscription;
    }
}
