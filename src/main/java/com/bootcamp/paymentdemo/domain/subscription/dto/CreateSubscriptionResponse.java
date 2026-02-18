package com.bootcamp.paymentdemo.domain.subscription.dto;

import com.bootcamp.paymentdemo.domain.subscription.entity.Subscription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionResponse {

    private String subscriptionId;
    private String customerUid;
    private String planId;
    private String paymentMethodId;
    private String status;
    private Long amount;
    private LocalDateTime currentPeriodEnd;

    public static CreateSubscriptionResponse from(Subscription subscription) {
        return new CreateSubscriptionResponse(
                subscription.getSubscriptionId(),
                subscription.getPaymentMethod().getCustomerUid(),
                subscription.getPlan().getPlanId(),
                subscription.getPaymentMethod().getPaymentMethodId(),
                subscription.getStatus().name(),
                subscription.getAmount(),
                subscription.getCurrentPeriodEnd()
        );
    }
}
