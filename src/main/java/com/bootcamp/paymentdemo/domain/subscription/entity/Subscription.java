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
@NoArgsConstructor
public class Subscription {

    @Id
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

    private Long amount;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status; // TRIALING, ACTIVE, CANCELLED 등

    private LocalDateTime currentPeriodEnd;
    private String reason;
    private String action;
    private boolean success;

    public static Subscription createSubscription(String id, Plan plan, Member member, PaymentMethod paymentMethod) {
        Subscription subscription = new Subscription();
        subscription.subscriptionId = id;
        subscription.plan = plan;
        subscription.member = member;
        subscription.paymentMethod = paymentMethod;
        subscription.status = SubscriptionStatus.TRIALING;
        return subscription;
    }
}
