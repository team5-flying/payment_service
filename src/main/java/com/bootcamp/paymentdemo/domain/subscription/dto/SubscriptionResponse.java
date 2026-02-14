package com.bootcamp.paymentdemo.domain.subscription.dto;

import com.bootcamp.paymentdemo.domain.subscription.entity.Subscription;
import com.bootcamp.paymentdemo.domain.subscription.entity.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private String subscriptionId;
    private String customerUid;
    private String planId;
    private String paymentMethodId;
    private SubscriptionStatus status;
    private Long amount;
    private LocalDateTime currentPeriodEnd;

    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getSubscriptionId(),
                subscription.getPaymentMethod().getCustomerUid(),
                subscription.getPlan().getPlanId(),
                subscription.getPaymentMethod().getPaymentMethodId(),
                subscription.getStatus(),
                subscription.getAmount(),
                subscription.getCurrentPeriodEnd()
        );
    }
}
