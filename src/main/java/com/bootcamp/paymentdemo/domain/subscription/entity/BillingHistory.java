package com.bootcamp.paymentdemo.domain.subscription.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "billing_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BillingHistory {

    @Id
    @Column(name = "billing_id")
    private String billingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id, nullable = false")
    private Subscription subscription;

    private String portOneId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingStatus status;

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime attemptDate;
    private String failureMessage;

    public static BillingHistory create(String billingId, Subscription subscription, String portOneId, Long amount, LocalDateTime periodStart, LocalDateTime periodEnd) {
        BillingHistory history = new BillingHistory();
        history.billingId = billingId;
        history.subscription = subscription;
        history.portOneId = portOneId;
        history.amount = amount;
        history.status = BillingStatus.PENDING;
        history.periodStart = periodStart;
        history.periodEnd = periodEnd;
        history.attemptDate = LocalDateTime.now();
        return history;
    }

    public void complete() {
        this.status = BillingStatus.COMPLETED;
    }

    public void fail(String message) {
        this.status = BillingStatus.FAILED;
        this.failureMessage = message;
    }
}
