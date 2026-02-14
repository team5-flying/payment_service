package com.bootcamp.paymentdemo.domain.subscription.entity;

import com.bootcamp.paymentdemo.domain.payment.entity.PaymentMethod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionInvoice {

    @Id
    private String billingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    private Long paymentId;
    private String amount;
    private LocalDateTime billingAt;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    public static SubscriptionInvoice createSubscriptionInvoice(String billingId, Subscription subscription, String amount, LocalDateTime start, LocalDateTime end) {
        SubscriptionInvoice invoice = new SubscriptionInvoice();
        invoice.billingId = billingId;
        invoice.subscription = subscription;
        invoice.amount = amount;
        invoice.periodStart = start;
        invoice.periodEnd = end;
        invoice.billingAt = LocalDateTime.now();
        return invoice;
    }
}
